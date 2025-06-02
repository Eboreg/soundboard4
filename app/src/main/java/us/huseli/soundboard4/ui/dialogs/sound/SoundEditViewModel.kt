package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.ui.SoundEditDestination
import javax.inject.Inject

@HiltViewModel
class SoundEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val soundRepository: SoundRepository,
    categoryRepository: CategoryRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    private val _destination = savedStateHandle.toRoute<SoundEditDestination>()
    private val _sounds = _destination.soundId?.let {
        soundRepository.flow(it).map { sound -> sound?.let { listOf(it) } ?: emptyList() }
    } ?: soundRepository.selectedSounds

    val sounds = _sounds.stateWhileSubscribed(emptyList())
    val categories = categoryRepository.flowAll().stateWhileSubscribed(emptyList())

    suspend fun save(params: SoundEditParams) {
        soundRepository.updateAll(
            _sounds.first().map { sound ->
                sound.copy(
                    name = params.name ?: sound.name,
                    volume = params.volume ?: sound.volume,
                    categoryId = params.categoryId ?: sound.categoryId,
                    playCount = if (params.resetPlayCount) 0 else sound.playCount,
                )
            }
        )
        undoRepository.pushUndoState()
    }
}

@Immutable
data class SoundEditParams(
    val name: String? = null,
    val volume: Float? = null,
    val categoryId: String? = null,
    val resetPlayCount: Boolean = false,
)
