package us.huseli.soundboard4.ui.dialogs.category

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.ui.CategoryDeleteDestination
import javax.inject.Inject

@HiltViewModel
class CategoryDeleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val soundRepository: SoundRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    private val _destination = savedStateHandle.toRoute<CategoryDeleteDestination>()

    val category = categoryRepository.flowOne(_destination.categoryId).stateWhileSubscribed()
    val sounds = soundRepository.flowByCategoryId(_destination.categoryId).stateWhileSubscribed(emptyList())

    suspend fun delete() {
        category.value?.also {
            soundRepository.deleteAll(sounds.value)
            categoryRepository.delete(it)
            undoRepository.pushUndoState()
        }
    }
}
