package us.huseli.soundboard4.ui.states

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.util.UUID

@Immutable
data class CategoryUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val isCollapsed: Boolean = false,
    val backgroundColor: Color = Color.Companion.White,
    val isFirst: Boolean = false,
    val isLast: Boolean = false,
    val soundCardUiStates: List<SoundCardUiState> = emptyList(),
) {
    val nameWithSoundCount: String = "$name (${soundCardUiStates.size})"
}
