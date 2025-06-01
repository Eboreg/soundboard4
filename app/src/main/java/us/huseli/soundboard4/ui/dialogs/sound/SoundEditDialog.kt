package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.ui.utils.CategoryDropdownMenu
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun SoundEditDialog(
    selectedCategoryId: String? = null,
    viewModel: SoundEditViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    onDismiss: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
) {
    val sounds by viewModel.sounds.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    SoundEditDialogImpl(
        sounds = sounds,
        categories = categories,
        onDismiss = onDismiss,
        onConfirm = { params ->
            scope.launch {
                wipState.run(
                    Dispatchers.IO,
                    context.resources.getQuantityString(R.plurals.saving_x_sounds, sounds.size, sounds.size),
                ) { viewModel.save(params) }
                onDismiss()
            }
        },
        onAddCategoryClick = onAddCategoryClick,
        selectedCategoryId = selectedCategoryId,
    )
}

@Composable
private fun SoundEditDialogImpl(
    sounds: List<Sound> = emptyList(),
    categories: List<Category> = emptyList(),
    selectedCategoryId: String? = null,
    onDismiss: () -> Unit = {},
    onConfirm: (SoundEditParams) -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
) {
    val singleSound = remember(sounds) { sounds.takeIf { it.size == 1 }?.first() }
    var name by rememberSaveable(singleSound) { mutableStateOf(singleSound?.name) }
    var volume by rememberSaveable(singleSound) { mutableFloatStateOf(singleSound?.volume ?: 1f) }
    var keepVolume by rememberSaveable(singleSound) { mutableStateOf(singleSound == null) }
    var resetPlayCount by rememberSaveable { mutableStateOf(false) }
    var categoryId by rememberSaveable(singleSound) { mutableStateOf(selectedCategoryId ?: singleSound?.categoryId) }
    val params = remember(name, volume, keepVolume, categoryId) {
        SoundEditParams(
            name = name,
            volume = volume.takeIf { !keepVolume },
            categoryId = categoryId,
            resetPlayCount = resetPlayCount,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onClick = { onConfirm(params) }) { Text(stringResource(R.string.save)) } },
        shape = MaterialTheme.shapes.small,
        title = { Text(pluralStringResource(R.plurals.edit_sound, sounds.size)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = singleSound?.let { name ?: "" } ?: stringResource(R.string.x_sounds_selected, sounds.size),
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    enabled = singleSound != null,
                )
                Column {
                    Text(stringResource(R.string.volume))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            enabled = !keepVolume,
                            modifier = Modifier.weight(1f),
                        )
                        if (singleSound == null) {
                            Checkbox(
                                checked = keepVolume,
                                onCheckedChange = { keepVolume = it },
                            )
                            Text(stringResource(R.string.no_change))
                        }
                    }
                }
                Column {
                    Text(stringResource(R.string.category))
                    CategoryDropdownMenu(
                        categories = categories,
                        selectedCategoryId = categoryId,
                        onSelect = { categoryId = it?.id },
                        showEmptyItem = singleSound == null,
                        emptyItemText = stringResource(R.string.not_changed),
                        onAddCategoryClick = onAddCategoryClick,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(pluralStringResource(R.plurals.reset_play_count, sounds.size))
                    Switch(checked = resetPlayCount, onCheckedChange = { resetPlayCount = it })
                }
            }
        }
    )
}
