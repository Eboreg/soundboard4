package us.huseli.soundboard4.ui.states

import android.net.Uri
import androidx.compose.runtime.Immutable
import us.huseli.soundboard4.data.database.model.Category

@Immutable
data class SettingsUiState(
    val autoImport: Boolean = false,
    val autoImportDirectory: Uri? = null,
    val autoImportCategory: Category? = null,
    val convertToWav: Boolean = true,
    val categories: List<Category> = emptyList(),
)
