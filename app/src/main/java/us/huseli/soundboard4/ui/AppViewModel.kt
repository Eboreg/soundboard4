package us.huseli.soundboard4.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.huseli.retaintheme.extensions.launchOnIOThread
import us.huseli.retaintheme.snackbar.SnackbarEngine
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.R
import us.huseli.soundboard4.RepressMode
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.domain.AutoSoundImportUseCase
import us.huseli.soundboard4.domain.CleanCacheAndOrphansUseCase
import us.huseli.soundboard4.domain.ManualSoundImportUseCase
import us.huseli.soundboard4.player.SoundPlayerRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val manualSoundImportUseCase: ManualSoundImportUseCase,
    private val soundRepository: SoundRepository,
    private val soundPlayerRepository: SoundPlayerRepository,
    private val autoSoundImportUseCase: AutoSoundImportUseCase,
    private val cleanCacheAndOrphansUseCase: CleanCacheAndOrphansUseCase,
) : AbstractBaseViewModel() {
    val isSelectEnabled = soundRepository.selectedSounds.map { it.isNotEmpty() }.stateWhileSubscribed(false)
    val repressMode = settingsRepository.repressMode
    val selectedSoundCount = soundRepository.selectedSounds.map { it.size }.stateWhileSubscribed(0)
    val totalSoundCount = soundRepository.filteredSounds.map { it.size }.stateWhileSubscribed(0)
    val canZoomIn = settingsRepository.canZoomIn.stateWhileSubscribed(true)
    val searchTerm = soundRepository.searchTerm

    init {
        launchOnIOThread {
            autoSoundImportUseCase()
            cleanCacheAndOrphansUseCase()
        }
    }

    fun deselectAllSounds() = soundRepository.deselectAllSounds()

    suspend fun importSoundsFromUris(uris: List<Uri>): Boolean = manualSoundImportUseCase(uris)

    fun selectAllSounds() {
        viewModelScope.launch {
            soundRepository.selectAllVisibleSounds()
        }
    }

    fun setOrientation(value: Int) = settingsRepository.setOrientation(value)

    fun setRepressMode(value: RepressMode) = settingsRepository.setRepressMode(value)

    fun setScreenWidthDp(value: Int) = settingsRepository.setScreenWidthDp(value)

    fun setSearchTerm(value: String?) = soundRepository.setSearchTerm(value)

    fun stopAllPlayers() = soundPlayerRepository.stopAllPlayers()

    fun zoomIn(context: Context) {
        settingsRepository.zoomIn().also {
            SnackbarEngine.addInfo(context.getString(R.string.zoom_x_percent, it))
        }
    }

    fun zoomOut(context: Context) {
        settingsRepository.zoomOut().also {
            SnackbarEngine.addInfo(context.getString(R.string.zoom_x_percent, it))
        }
    }
}
