package us.huseli.soundboard4.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.huseli.soundboard4.data.database.dao.CategoryDao
import us.huseli.soundboard4.data.database.dao.SoundDao
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.IModel
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.data.database.model.diff
import javax.inject.Inject
import javax.inject.Singleton

data class UndoState(
    // Sets, because internal order should not matter when comparing
    val categories: Set<Category>,
    val sounds: Set<Sound>,
) {
    data class Diff(val categories: IModel.Diff<Category>, val sounds: IModel.Diff<Sound>)

    fun diff(other: UndoState): Diff = Diff(
        categories = categories.diff(other.categories),
        sounds = sounds.diff(other.sounds),
    )
}

@Singleton
class UndoRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val soundDao: SoundDao,
    private val coroutineScope: CoroutineScope,
) {
    private val _currentUndoStateIdx = MutableStateFlow(-1)
    private val _undoStates = MutableStateFlow<List<UndoState>>(emptyList())
    private val _writeLock = Mutex()

    val canRedo = combine(_undoStates, _currentUndoStateIdx) { states, currentIdx ->
        states.lastIndex > currentIdx
    }
    val canUndo = _currentUndoStateIdx.map { it > 0 }

    init {
        coroutineScope.launch(Dispatchers.IO) { pushUndoState() }
    }

    fun getCurrentUndoState(): UndoState? =
        _currentUndoStateIdx.value.takeIf { it > -1 }?.let { _undoStates.value.getOrNull(it) }

    fun getNextUndoState(): UndoState? =
        _currentUndoStateIdx.value.takeIf { it > -1 }?.let { _undoStates.value.getOrNull(it + 1) }

    fun getPreviousUndoState(): UndoState? =
        _currentUndoStateIdx.value.takeIf { it > 0 }?.let { _undoStates.value.getOrNull(it - 1) }

    suspend fun pushUndoState() {
        val newState = UndoState(
            categories = categoryDao.listAll().toSet(),
            sounds = soundDao.listAll().toSet(),
        )

        _writeLock.withLock {
            val currentState = getCurrentUndoState()

            if (newState != currentState) {
                _undoStates.value =
                    _undoStates.value.filterIndexed { idx, _ -> idx <= _currentUndoStateIdx.value }.plus(newState)
                if (_undoStates.value.size > MAX_UNDO_STATES) {
                    _undoStates.value = _undoStates.value.drop(_undoStates.value.size - MAX_UNDO_STATES)
                }
                _currentUndoStateIdx.value = _undoStates.value.lastIndex
            }
        }
    }

    fun redo() {
        coroutineScope.launch(Dispatchers.IO) {
            _writeLock.withLock {
                getNextUndoState()?.also { state ->
                    applyUndoState(state)
                    _currentUndoStateIdx.value++
                }
            }
        }
    }

    fun undo() {
        coroutineScope.launch(Dispatchers.IO) {
            _writeLock.withLock {
                getPreviousUndoState()?.also { state ->
                    applyUndoState(state)
                    _currentUndoStateIdx.value--
                }
            }
        }
    }

    private suspend fun applyUndoState(state: UndoState) {
        val currentState = UndoState(
            categories = categoryDao.listAll().toSet(),
            sounds = soundDao.listAll().toSet(),
        )
        val diff = state.diff(currentState)

        // Make sure to insert new categories before updating/inserting any sounds:
        if (diff.categories.new.isNotEmpty()) categoryDao.insertAll(diff.categories.new)
        if (diff.categories.changed.isNotEmpty()) categoryDao.updateAll(diff.categories.changed)
        if (diff.sounds.new.isNotEmpty()) soundDao.insertAll(diff.sounds.new)
        // Also to update and delete sounds before deleting categories:
        if (diff.sounds.deleted.isNotEmpty()) soundDao.deleteAll(diff.sounds.deleted)
        if (diff.sounds.changed.isNotEmpty()) soundDao.updateAll(diff.sounds.changed)
        if (diff.categories.deleted.isNotEmpty()) categoryDao.deleteAll(diff.categories.deleted)
    }

    companion object {
        const val MAX_UNDO_STATES = 50
    }
}
