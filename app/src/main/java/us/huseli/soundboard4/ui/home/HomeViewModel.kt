package us.huseli.soundboard4.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import us.huseli.retaintheme.extensions.launchOnIOThread
import us.huseli.retaintheme.extensions.listItemsBetween
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.Constants.SOUND_DURATION_FONT_SIZE
import us.huseli.soundboard4.Constants.SOUND_DURATION_FONT_SIZE_LARGE
import us.huseli.soundboard4.Constants.SOUND_NAME_FONT_SIZE
import us.huseli.soundboard4.Constants.SOUND_NAME_FONT_SIZE_LARGE
import us.huseli.soundboard4.data.database.model.SoundComparator
import us.huseli.soundboard4.data.database.model.filterBySearchTerm
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.data.repository.UndoRepository
import us.huseli.soundboard4.player.SoundPlayerRepository
import us.huseli.soundboard4.ui.states.CategoryUiState
import us.huseli.soundboard4.ui.states.HomeUiState
import us.huseli.soundboard4.ui.states.SoundCardUiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val soundRepository: SoundRepository,
    private val categoryRepository: CategoryRepository,
    private val soundPlayerRepository: SoundPlayerRepository,
    private val undoRepository: UndoRepository,
) : AbstractBaseViewModel() {
    val homeUiState = combine(
        categoryRepository.flowAllMultimaps(),
        settingsRepository.repressMode,
        soundRepository.selectedSounds,
        settingsRepository.columnInfo,
        soundRepository.searchTerm,
    ) { multimap, repressMode, selectedSounds, columnInfo, searchTerm ->
        val isSelectEnabled = selectedSounds.isNotEmpty()
        val soundNameFontSize = if (columnInfo.widthDp > 150) SOUND_NAME_FONT_SIZE_LARGE.sp else SOUND_NAME_FONT_SIZE.sp
        val soundDurationFontSize =
            if (columnInfo.widthDp > 150) SOUND_DURATION_FONT_SIZE_LARGE.sp else SOUND_DURATION_FONT_SIZE.sp

        HomeUiState(
            repressMode = repressMode,
            columnCount = columnInfo.count,
            categoryUiStates = multimap.toList().let { list ->
                list.mapIndexed { index, (category, sounds) ->
                    val backgroundColor = Color(category.backgroundColor)

                    CategoryUiState(
                        backgroundColor = backgroundColor,
                        isFirst = index == 0,
                        isLast = index == list.lastIndex,
                        id = category.id,
                        name = category.name,
                        isCollapsed = category.isCollapsed,
                        soundCardUiStates = sounds
                            .filterBySearchTerm(searchTerm)
                            .sortedWith(SoundComparator(category.sortingKey, category.sortAscending)).map { sound ->
                                SoundCardUiState(
                                    sound = sound,
                                    backgroundColor = backgroundColor,
                                    repressMode = repressMode,
                                    isSelectEnabled = isSelectEnabled,
                                    isSelected = selectedSounds.contains(sound),
                                    nameFontSize = soundNameFontSize,
                                    durationFontSize = soundDurationFontSize,
                                    player = soundPlayerRepository.getSoundPlayer(sound),
                                )
                            }
                    )
                }
            }
        )
    }.flowOn(Dispatchers.IO).stateWhileSubscribed(HomeUiState())

    fun deselectSound(soundId: String) = soundRepository.deselectSound(soundId)

    fun moveCategoryDown(categoryId: String) {
        launchOnIOThread {
            categoryRepository.moveDown(categoryId)
            undoRepository.pushUndoState()
        }
    }

    fun moveCategoryUp(categoryId: String) {
        launchOnIOThread {
            categoryRepository.moveUp(categoryId)
            undoRepository.pushUndoState()
        }
    }

    fun selectSound(soundId: String) = soundRepository.selectSound(soundId)

    fun selectSoundsUntil(soundId: String) {
        viewModelScope.launch {
            val visibleSoundIds = homeUiState.first().categoryUiStates
                .filter { !it.isCollapsed }
                .flatMap { it.soundCardUiStates }
                .map { it.id }

            soundRepository.selectSounds(
                soundRepository.selectedSounds.first().map { it.id }.toMutableSet().apply {
                    lastOrNull()?.also { addAll(visibleSoundIds.listItemsBetween(it, soundId)) }
                    add(soundId)
                }
            )
        }
    }

    fun setCategoryCollapsed(categoryId: String, collapsed: Boolean) {
        launchOnIOThread {
            categoryRepository.setCollapsed(categoryId, collapsed)
        }
    }
}
