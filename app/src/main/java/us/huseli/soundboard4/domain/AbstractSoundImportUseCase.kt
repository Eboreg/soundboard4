package us.huseli.soundboard4.domain

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.arthenica.ffmpegkit.FFmpegKit
import dagger.hilt.android.qualifiers.ApplicationContext
import us.huseli.soundboard4.copyTo
import us.huseli.soundboard4.data.model.TempSound
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.getSoundCacheDirectory
import us.huseli.soundboard4.isWavMimeType
import us.huseli.soundboard4.md5
import java.io.File
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class SoundMetadata(
    val uri: Uri,
    val id: String,
    val name: String?,
    val duration: Duration,
    val mimeType: String,
    val checksum: String,
) {
    val basename = uri.path!!.substringAfterLast('/')
    val stem = basename.substringBeforeLast('.')
    val suffix = basename.substringAfterLast('.').let { if (it.isNotBlank()) ".$it" else "" }
    val outStem = "$stem-$id"
}

abstract class AbstractSoundImportUseCase(
    @ApplicationContext protected val context: Context,
    protected val soundRepository: SoundRepository,
    protected val settingsRepository: SettingsRepository,
) {
    protected fun copyContents(uri: Uri, outFile: File) {
        context.contentResolver.openInputStream(uri).use { input ->
            outFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
    }

    protected fun convertAndCopy(metadata: SoundMetadata, dir: File): File {
        val outFileName =
            if (settingsRepository.convertToWav.value) "${metadata.outStem}.wav"
            else "${metadata.outStem}${metadata.suffix}"
        val outFile = File(dir, outFileName)

        if (!metadata.mimeType.isWavMimeType() && settingsRepository.convertToWav.value) {
            val tempFile = File(context.getSoundCacheDirectory(), metadata.uri.path!!.substringAfterLast('/'))
                .also { copyContents(metadata.uri, it) }
            val session = FFmpegKit.execute("-i \"${tempFile.path}\" -y \"${outFile.path}\"")

            tempFile.delete()
            if (session.returnCode.isValueError) throw Error(session.logsAsString)
        } else {
            copyContents(metadata.uri, outFile)
        }
        return outFile
    }

    protected suspend fun getExistingChecksums(): Set<String> =
        soundRepository.listAll().map { it.checksum }.toSet()

    protected fun getSoundMetadata(uri: Uri): SoundMetadata {
        val retriever = MediaMetadataRetriever().also { it.setDataSource(context, uri) }
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()?.milliseconds
        val metadataName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        val name = metadataName ?: context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                cursor.moveToFirst()
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { index ->
                    cursor.getString(index).substringBeforeLast('.')
                }
            }
        val checksum = context.contentResolver.openInputStream(uri).use { it!!.md5() }

        return SoundMetadata(
            uri = uri,
            id = UUID.randomUUID().toString(),
            name = name,
            duration = duration!!,
            mimeType = mimeType!!,
            checksum = checksum,
        ).also { retriever.close() }
    }

    protected fun uriToTempSound(uri: Uri, existingChecksums: Collection<String>): TempSound {
        val metadata = getSoundMetadata(uri)
        val outFile = convertAndCopy(metadata, context.getSoundCacheDirectory())

        return TempSound(
            id = metadata.id,
            name = metadata.name ?: metadata.stem,
            file = outFile,
            duration = metadata.duration,
            checksum = metadata.checksum,
            isDuplicate = existingChecksums.contains(metadata.checksum),
            mimeType = metadata.mimeType,
        )
    }
}
