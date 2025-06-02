package us.huseli.soundboard4.domain

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.TempSoundRepository
import us.huseli.soundboard4.getAnnotatedString
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import javax.inject.Inject

class ManualSoundImportUseCase @Inject constructor(
    @ApplicationContext context: Context,
    soundRepository: SoundRepository,
    private val tempSoundRepository: TempSoundRepository,
    settingsRepository: SettingsRepository,
) : AbstractSoundImportUseCase(context, soundRepository, settingsRepository) {
    suspend operator fun invoke(uris: List<Uri>, wipState: WorkInProgressState? = null): Boolean {
        val existingChecksums = getExistingChecksums()

        tempSoundRepository.clear()

        for (uri in uris) {
            uri.path?.substringAfterLast('/')?.also {
                wipState?.addStatusRow(context.getAnnotatedString(R.string.importing_x, it))
            }
            try {
                tempSoundRepository.tempSounds.value += uriToTempSound(uri, existingChecksums)
            } catch (e: Throwable) {
                tempSoundRepository.errors.value += e.toString()
            }
        }

        return tempSoundRepository.isNotEmpty()
    }
}
