package us.huseli.soundboard4.ui.dialogs.sound

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.ui.SoundDeleteDestination
import javax.inject.Inject

@HiltViewModel
class SoundDeleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val soundRepository: SoundRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    private val _destination = savedStateHandle.toRoute<SoundDeleteDestination>()
    private val _sounds = _destination.soundId?.let {
        soundRepository.flow(it).map { sound -> sound?.let { listOf(it) } ?: emptyList() }
    } ?: soundRepository.selectedSounds

    val soundCount = _sounds.map { it.size }.stateWhileSubscribed(0)
    val name =
        _sounds.map { sounds -> sounds.takeIf { it.size == 1 }?.first()?.name }.stateWhileSubscribed()

    suspend fun delete() {
        soundRepository.deleteAll(_sounds.first())
        soundRepository.deselectAllSounds()
        undoRepository.pushUndoState()
    }
}
