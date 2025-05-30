package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import us.huseli.soundboard4.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTopBar(
    selectedSoundCount: Int,
    totalSoundCount: Int,
    onDisableSelectClick: () -> Unit = {},
    onSelectAllClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDisableSelectClick) {
                    Icon(Icons.AutoMirrored.Sharp.ArrowBack, stringResource(R.string.exit_selection_mode))
                }
                Text(stringResource(R.string.x_of_y_selected, selectedSoundCount, totalSoundCount))
            }
        },
        actions = {
            IconButton(onClick = onSelectAllClick) {
                Icon(Icons.Sharp.SelectAll, stringResource(R.string.select_all_sounds))
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Sharp.Edit, pluralStringResource(R.plurals.edit_sound, selectedSoundCount))
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Sharp.Delete, pluralStringResource(R.plurals.delete_sound, selectedSoundCount))
            }
        },
    )
}
