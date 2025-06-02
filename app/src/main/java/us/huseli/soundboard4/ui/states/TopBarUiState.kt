package us.huseli.soundboard4.ui.states

import androidx.compose.runtime.Immutable
import us.huseli.soundboard4.RepressMode

@Immutable
data class TopBarUiState(
    val repressMode: RepressMode = RepressMode.STOP,
    val canZoomIn: Boolean = true,
    val searchTerm: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
)
