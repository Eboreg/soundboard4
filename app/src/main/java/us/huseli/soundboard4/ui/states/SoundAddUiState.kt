package us.huseli.soundboard4.ui.states

import androidx.compose.runtime.Immutable
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.model.TempSound

@Immutable
data class SoundAddUiState(
    val categories: List<Category> = emptyList(),
    val newSoundCount: Int = 0,
    val duplicateSoundCount: Int = 0,
    val errors: List<String> = emptyList(),
    val includeDuplicates: Boolean = false,
    val singleSound: TempSound? = null,
) {
    val totalSoundCount: Int = newSoundCount + duplicateSoundCount
}
