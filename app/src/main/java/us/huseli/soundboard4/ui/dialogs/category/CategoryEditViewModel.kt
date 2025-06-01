package us.huseli.soundboard4.ui.dialogs.category

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.randomColor
import us.huseli.soundboard4.ui.CategoryEditDestination
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    private val _destination = savedStateHandle.toRoute<CategoryEditDestination>()
    private val _isNew = MutableStateFlow(_destination.categoryId == null)

    val category: StateFlow<Category> = _destination.categoryId
        ?.let { categoryRepository.flowOne(it).filterNotNull().stateWhileSubscribed(Category()) }
        ?: MutableStateFlow(Category(backgroundColor = randomColor()))
    val isNew = _isNew.asStateFlow()

    suspend fun save(category: Category) {
        if (_isNew.value) categoryRepository.insert(category)
        else categoryRepository.update(category)
        undoRepository.pushUndoState()
    }
}
