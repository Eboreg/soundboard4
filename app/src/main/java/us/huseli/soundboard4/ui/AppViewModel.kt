package us.huseli.soundboard4.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.domain.AutoSoundImportUseCase
import us.huseli.soundboard4.domain.CleanCacheAndOrphansUseCase
import us.huseli.soundboard4.domain.ManualSoundImportUseCase
import us.huseli.soundboard4.player.SoundPlayerRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val autoSoundImportUseCase: AutoSoundImportUseCase,
    cleanCacheAndOrphansUseCase: CleanCacheAndOrphansUseCase,
) : AbstractBaseViewModel() {
    init {
        cleanCacheAndOrphansUseCase(onFinish = { autoSoundImportUseCase() })
    }

    fun setOrientation(value: Int) = settingsRepository.setOrientation(value)

    fun setScreenWidthDp(value: Int) = settingsRepository.setScreenWidthDp(value)
}
