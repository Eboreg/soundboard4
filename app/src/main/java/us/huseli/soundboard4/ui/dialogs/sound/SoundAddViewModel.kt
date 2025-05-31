package us.huseli.soundboard4.ui.dialogs.sound

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.TempSoundRepository
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import javax.inject.Inject

@HiltViewModel
class SoundAddViewModel @Inject constructor(
    private val soundRepository: SoundRepository,
    private val tempSoundRepository: TempSoundRepository,
    categoryRepository: CategoryRepository,
) : AbstractBaseViewModel() {
    val categories = categoryRepository.flowAll().stateWhileSubscribed(emptyList())
    val newSounds = tempSoundRepository.tempSounds
        .map { sounds -> sounds.filter { !it.isDuplicate } }
        .stateWhileSubscribed(emptyList())
    val duplicateSounds = tempSoundRepository.tempSounds
        .map { sounds -> sounds.filter { it.isDuplicate } }
        .stateWhileSubscribed(emptyList())
    val errors = tempSoundRepository.errors.asStateFlow()

    suspend fun save(
        categoryId: String,
        singleSoundName: String?,
        includeDuplicates: Boolean,
        wipState: WorkInProgressState? = null,
    ) {
        val toImport = tempSoundRepository.tempSounds.value
            .filter { includeDuplicates || !it.isDuplicate }
        val sounds = tempSoundRepository.convertToSounds(toImport, categoryId, wipState)

        if (toImport.size == 1 && singleSoundName != null) {
            soundRepository.insert(sounds.first().copy(name = singleSoundName))
        } else {
            soundRepository.insertAll(sounds)
        }
        tempSoundRepository.clear()
    }
}
