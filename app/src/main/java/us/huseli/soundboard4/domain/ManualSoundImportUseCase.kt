package us.huseli.soundboard4.domain

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.TempSoundRepository
import javax.inject.Inject

class ManualSoundImportUseCase @Inject constructor(
    @ApplicationContext context: Context,
    soundRepository: SoundRepository,
    private val tempSoundRepository: TempSoundRepository,
    settingsRepository: SettingsRepository,
) : AbstractSoundImportUseCase(context, soundRepository, settingsRepository) {
    suspend operator fun invoke(uris: List<Uri>): Boolean {
        val existingChecksums = getExistingChecksums()

        tempSoundRepository.clear()

        for (uri in uris) {
            try {
                tempSoundRepository.tempSounds.value += uriToTempSound(uri, existingChecksums)
            } catch (e: Throwable) {
                tempSoundRepository.errors.value += e.toString()
            }
        }

        return tempSoundRepository.isNotEmpty()
    }
}
