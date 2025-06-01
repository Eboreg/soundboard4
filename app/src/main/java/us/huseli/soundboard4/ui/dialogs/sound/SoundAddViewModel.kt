package us.huseli.soundboard4.ui.dialogs.sound

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.model.duplicates
import us.huseli.soundboard4.data.model.nonDuplicates
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.TempSoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.ui.states.SoundAddUiState
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import javax.inject.Inject

@HiltViewModel
class SoundAddViewModel @Inject constructor(
    private val soundRepository: SoundRepository,
    private val tempSoundRepository: TempSoundRepository,
    categoryRepository: CategoryRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    private val _includeDuplicates = MutableStateFlow(false)

    val uiState = combine(
        categoryRepository.flowAll(),
        tempSoundRepository.tempSounds,
        tempSoundRepository.errors,
        _includeDuplicates,
    ) { categories, sounds, errors, includeDuplicates ->
        SoundAddUiState(
            categories = categories,
            newSoundCount = sounds.nonDuplicates().size,
            duplicateSoundCount = sounds.duplicates().size,
            errors = errors,
            includeDuplicates = includeDuplicates,
            singleSound = sounds.takeIf { it.size == 1 }?.first(),
        )
    }.stateWhileSubscribed(SoundAddUiState())

    suspend fun save(
        categoryId: String,
        singleSoundName: String?,
        wipState: WorkInProgressState? = null,
    ) {
        val toImport = tempSoundRepository.tempSounds.value
            .filter { _includeDuplicates.value || !it.isDuplicate }
        val sounds = tempSoundRepository.convertToSounds(toImport, categoryId, wipState)

        if (toImport.size == 1 && singleSoundName != null) {
            soundRepository.insert(sounds.first().copy(name = singleSoundName))
        } else {
            soundRepository.insertAll(sounds)
        }
        tempSoundRepository.clear()
        undoRepository.pushUndoState()
    }

    fun setIncludeDuplicates(value: Boolean) {
        _includeDuplicates.value = value
    }
}
