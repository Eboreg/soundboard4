package us.huseli.soundboard4.ui.dialogs.category

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.SoundSortingKey
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.getAnnotatedString
import us.huseli.soundboard4.ui.dialogs.ColorPickerDialog
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import us.huseli.soundboard4.ui.utils.SimpleExposedDropdownMenu
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun CategoryEditDialog(
    viewModel: CategoryEditViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    onDismiss: () -> Unit = {},
    onCreated: (Category) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val isNew by viewModel.isNew.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CategoryEditDialogImpl(
        category = category,
        isNew = isNew,
        onDismiss = onDismiss,
        onSave = { category ->
            scope.launch {
                wipState.run(Dispatchers.IO, context.getAnnotatedString(R.string.saving_x, category.name)) {
                    viewModel.save(category)
                }
                if (isNew) onCreated(category)
                onDismiss()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditDialogImpl(
    category: Category,
    isNew: Boolean = false,
    onDismiss: () -> Unit = {},
    onSave: (Category) -> Unit = {},
) {
    var name by rememberSaveable(category.name) { mutableStateOf(category.name) }
    var backgroundColor by rememberSaveable(category.backgroundColor) { mutableIntStateOf(category.backgroundColor) }
    var isColorPickerOpen by rememberSaveable { mutableStateOf(false) }
    var sortingKey by remember(category.sortingKey) { mutableStateOf(category.sortingKey) }
    var sortAscending by rememberSaveable(category.sortAscending) { mutableStateOf(category.sortAscending) }
    val isValid = remember(name) { name.isNotBlank() }

    if (isColorPickerOpen) {
        ColorPickerDialog(
            initialColor = Color(backgroundColor),
            onConfirm = {
                backgroundColor = it.toArgb()
                isColorPickerOpen = false
            },
            onDismiss = { isColorPickerOpen = false },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        category.copy(
                            name = name,
                            backgroundColor = backgroundColor,
                            sortingKey = sortingKey,
                            sortAscending = sortAscending,
                        )
                    )
                },
                enabled = isValid,
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(if (isNew) stringResource(R.string.add_category) else category.name) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(backgroundColor),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        content = {},
                        modifier = Modifier.size(36.dp)
                    )
                    OutlinedButton(
                        onClick = { isColorPickerOpen = true },
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.select_background_colour)) }
                }

                Column {
                    SimpleExposedDropdownMenu(
                        values = SoundSortingKey.entries,
                        initialValue = sortingKey,
                        onSelect = { sortingKey = it!! },
                        label = { Text(stringResource(R.string.sort_sounds_by)) },
                        item = { Text(stringResource(it!!.resId)) },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.selectableGroup()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sortAscending, onClick = { sortAscending = true })
                            Text(
                                stringResource(R.string.ascending),
                                modifier = Modifier.clickable { sortAscending = true }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !sortAscending, onClick = { sortAscending = false })
                            Text(
                                stringResource(R.string.descending),
                                modifier = Modifier.clickable { sortAscending = false }
                            )
                        }
                    }
                }
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CategoryEditDialogPreview() {
    Soundboard4Theme {
        CategoryEditDialogImpl(
            category = Category(name = "category 1", backgroundColor = Color.Red.toArgb()),
        )
    }
}
