package us.huseli.soundboard4.ui.states

import androidx.compose.runtime.Immutable
import us.huseli.soundboard4.Constants.DEFAULT_COLUMN_COUNT_PORTRAIT
import us.huseli.soundboard4.RepressMode

@Immutable
data class HomeUiState(
    val categoryUiStates: List<CategoryUiState> = emptyList(),
    val repressMode: RepressMode = RepressMode.STOP,
    val columnCount: Int = DEFAULT_COLUMN_COUNT_PORTRAIT,
)
