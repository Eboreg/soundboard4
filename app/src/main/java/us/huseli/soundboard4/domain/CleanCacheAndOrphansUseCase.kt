package us.huseli.soundboard4.domain

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.getInternalSoundDirectory
import us.huseli.soundboard4.getSoundCacheDirectory
import javax.inject.Inject

class CleanCacheAndOrphansUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundRepository: SoundRepository,
    private val coroutineScope: CoroutineScope,
) {
    operator fun invoke(onFinish: (() -> Unit)? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            invokeSuspending()
            onFinish?.invoke()
        }
    }

    suspend fun invokeSuspending(): Int {
        val soundUris = soundRepository.listAll().map { it.uri }
        val files = context.getInternalSoundDirectory().listFiles()
        val cacheFiles = context.getSoundCacheDirectory().listFiles()
        var deletedFiles = 0

        files?.filterNotNull()?.forEach { file ->
            if (!soundUris.contains(file.toUri().toString())) {
                file.delete()
                deletedFiles++
            }
        }

        cacheFiles?.filterNotNull()?.forEach { file ->
            file.delete()
            deletedFiles++
        }

        return deletedFiles
    }
}
