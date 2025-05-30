package us.huseli.soundboard4.data.repository

import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import us.huseli.soundboard4.data.database.dao.SoundDao
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.data.database.model.filterBySearchTerm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor(private val soundDao: SoundDao) {
    private val _searchTerm = MutableStateFlow<String?>(null)
    private val _selectedSoundIds = MutableStateFlow<Set<String>>(emptySet())

    val filteredSounds = combine(soundDao.flowAll(), _searchTerm) { sounds, searchTerm ->
        sounds.filterBySearchTerm(searchTerm)
    }
    val searchTerm = _searchTerm.asStateFlow()
    val selectedSounds = combine(_selectedSoundIds, filteredSounds) { selectedSoundIds, sounds ->
        sounds.filter { selectedSoundIds.contains(it.id) }
    }

    suspend fun deleteAll(sounds: List<Sound>) {
        for (sound in sounds) {
            val uri = sound.uri.toUri().buildUpon().scheme("file").build()
            uri.toFile().delete()
        }
        soundDao.deleteAll(sounds)
    }

    fun deselectAllSounds() {
        _selectedSoundIds.value = emptySet()
    }

    fun deselectSound(soundId: String) {
        _selectedSoundIds.value -= soundId
    }

    fun flowByCategoryId(categoryId: String): Flow<List<Sound>> = soundDao.flowByCategoryId(categoryId)

    suspend fun increasePlayCount(soundId: String) = soundDao.increasePlayCount(soundId)

    suspend fun insert(sound: Sound): Long = soundDao.insert(sound)

    suspend fun insertAll(sounds: Collection<Sound>) = soundDao.insertAll(sounds)

    suspend fun listAll(): List<Sound> = soundDao.listAll()

    suspend fun selectAllVisibleSounds() {
        _selectedSoundIds.value = soundDao.listAll().filterBySearchTerm(_searchTerm.value).map { it.id }.toSet()
    }

    fun selectSound(soundId: String) {
        _selectedSoundIds.value += soundId
    }

    fun selectSounds(soundIds: Collection<String>) {
        _selectedSoundIds.value += soundIds
    }

    fun setSearchTerm(value: String?) {
        _searchTerm.value = value
    }

    suspend fun update(sound: Sound) = soundDao.update(sound)

    suspend fun updateAll(sounds: List<Sound>) = soundDao.updateAll(sounds)
}
