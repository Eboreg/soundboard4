package us.huseli.soundboard4.domain

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.retaintheme.snackbar.SnackbarEngine
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.getInternalSoundDirectory
import javax.inject.Inject

class AutoSoundImportUseCase @Inject constructor(
    @ApplicationContext context: Context,
    soundRepository: SoundRepository,
    settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val coroutineScope: CoroutineScope,
) : AbstractSoundImportUseCase(context, soundRepository, settingsRepository) {
    operator fun invoke(onFinish: (() -> Unit)? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            invokeSuspending()
            onFinish?.invoke()
        }
    }

    suspend fun invokeSuspending() {
        val categories = categoryRepository.listAll()
        val categoryId = settingsRepository.autoImportCategoryId.value
            ?.takeIf { categories.map { it.id }.contains(it) }
            ?: categories.firstOrNull()?.id
        val dirUri = settingsRepository.autoImportDirectory.value
        var importedSounds = 0
        val errors = mutableListOf<Throwable>()

        if (dirUri != null && categoryId != null && settingsRepository.autoImport.value) {
            val existingChecksums = getExistingChecksums()
            val documentFiles = try {
                DocumentFile.fromTreeUri(context, dirUri)?.listFiles()
            } catch (e: Throwable) {
                errors.add(e)
                null
            }

            documentFiles?.forEach { documentFile ->
                try {
                    if (documentFile.type?.startsWith("audio/") == true) {
                        val metadata = getSoundMetadata(documentFile.uri)

                        if (!existingChecksums.contains(metadata.checksum)) {
                            val outFile = convertAndCopy(metadata, context.getInternalSoundDirectory())

                            soundRepository.insert(
                                Sound(
                                    id = metadata.id,
                                    name = metadata.name ?: metadata.stem,
                                    uri = outFile.toUri().toString(),
                                    duration = metadata.duration,
                                    categoryId = categoryId,
                                    checksum = metadata.checksum,
                                    mimeType = metadata.mimeType,
                                )
                            )
                            importedSounds++
                        }
                    }
                } catch (e: Throwable) {
                    errors.add(e)
                }
            }
        }

        if (importedSounds > 0) SnackbarEngine.addInfo(
            context.resources.getQuantityString(
                R.plurals.x_new_sounds_were_auto_imported,
                importedSounds,
                importedSounds,
            )
        )
        if (errors.isNotEmpty()) SnackbarEngine.addError(
            context.getString(R.string.error_s_when_auto_importing_sounds, errors)
        )
    }
}
