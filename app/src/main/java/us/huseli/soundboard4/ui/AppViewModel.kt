package us.huseli.soundboard4.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import us.huseli.retaintheme.extensions.launchOnIOThread
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.domain.AutoSoundImportUseCase
import us.huseli.soundboard4.domain.CleanCacheAndOrphansUseCase
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val autoSoundImportUseCase: AutoSoundImportUseCase,
    cleanCacheAndOrphansUseCase: CleanCacheAndOrphansUseCase,
    categoryRepository: CategoryRepository,
) : AbstractBaseViewModel() {
    val isFirstRun = settingsRepository.isFirstRun

    init {
        cleanCacheAndOrphansUseCase(onFinish = { autoSoundImportUseCase() })
        if (isFirstRun.value) launchOnIOThread { categoryRepository.insertDefault() }
    }

    fun setOrientation(value: Int) = settingsRepository.setOrientation(value)

    fun setScreenWidthDp(value: Int) = settingsRepository.setScreenWidthDp(value)

    fun setIsFirstRun(value: Boolean) = settingsRepository.setIsFirstRun(value)
}
