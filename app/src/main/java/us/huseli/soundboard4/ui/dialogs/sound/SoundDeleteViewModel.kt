package us.huseli.soundboard4.ui.dialogs.sound

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.SoundRepository
import javax.inject.Inject

@HiltViewModel
class SoundDeleteViewModel @Inject constructor(
    private val soundRepository: SoundRepository,
) : AbstractBaseViewModel() {
    val soundCount = soundRepository.selectedSounds.map { it.size }.stateWhileSubscribed(0)

    suspend fun delete() {
        soundRepository.deleteAll(soundRepository.selectedSounds.first())
    }
}
