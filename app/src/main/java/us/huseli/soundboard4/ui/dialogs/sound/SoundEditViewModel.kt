package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.runtime.Immutable
import dagger.hilt.android.lifecycle.HiltViewModel
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import javax.inject.Inject

@HiltViewModel
class SoundEditViewModel @Inject constructor(
    private val soundRepository: SoundRepository,
    categoryRepository: CategoryRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    val sounds = soundRepository.selectedSounds.stateWhileSubscribed(emptyList())
    val categories = categoryRepository.flowAll().stateWhileSubscribed(emptyList())

    suspend fun save(params: SoundEditParams) {
        soundRepository.updateAll(
            sounds.value.map { sound ->
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
