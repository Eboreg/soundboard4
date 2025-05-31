package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.model.TempSound
import us.huseli.soundboard4.ui.utils.CategoryDropdownMenu
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun SoundAddDialog(
    selectedCategoryId: String? = null,
    viewModel: SoundAddViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    onDismiss: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val sounds by viewModel.newSounds.collectAsStateWithLifecycle()
    val duplicateSounds by viewModel.duplicateSounds.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    SoundAddDialogImpl(
        sounds = sounds,
        duplicateSounds = duplicateSounds,
        errors = errors,
        categories = categories,
        onDismiss = onDismiss,
        onConfirmClick = { categoryId, singleSoundName, includeDuplicates ->
            scope.launch {
                wipState.run(Dispatchers.IO) {
                    viewModel.save(categoryId, singleSoundName, includeDuplicates, wipState)
                }
                onDismiss()
            }
        },
        onAddCategoryClick = onAddCategoryClick,
        selectedCategoryId = selectedCategoryId,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundAddDialogImpl(
    sounds: List<TempSound> = emptyList(),
    duplicateSounds: List<TempSound> = emptyList(),
    errors: List<String> = emptyList(),
    categories: List<Category> = emptyList(),
    selectedCategoryId: String? = null,
    onDismiss: () -> Unit = {},
    onConfirmClick: (String, String?, Boolean) -> Unit = { _, _, _ -> },
    onAddCategoryClick: () -> Unit = {},
) {
    var categoryId by remember(categories) { mutableStateOf(selectedCategoryId ?: categories.firstOrNull()?.id) }
    var includeDuplicates by remember { mutableStateOf(false) }

    val selectedSoundCount = remember(sounds, duplicateSounds, includeDuplicates) {
        sounds.size + if (includeDuplicates) duplicateSounds.size else 0
    }
    val confirmButtonEnabled = remember(selectedSoundCount, categoryId) {
        categoryId != null && selectedSoundCount > 0
    }
    val singleSound = remember(selectedSoundCount, sounds, duplicateSounds) {
        (sounds.firstOrNull() ?: duplicateSounds.firstOrNull())?.takeIf { selectedSoundCount == 1 }
    }

    var singleSoundName by remember(singleSound) { mutableStateOf(singleSound?.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirmClick(categoryId!!, singleSoundName, includeDuplicates) },
                enabled = confirmButtonEnabled,
            ) { Text(stringResource(R.string.import_)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        shape = MaterialTheme.shapes.small,
        title = { Text(pluralStringResource(R.plurals.add_sound, selectedSoundCount)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (sounds.isNotEmpty())
                    Text(pluralStringResource(R.plurals.x_new_sounds_to_import, sounds.size, sounds.size))
                if (duplicateSounds.isNotEmpty())
                    Text(pluralStringResource(R.plurals.x_duplicate_sounds, duplicateSounds.size, duplicateSounds.size))
                if (errors.isNotEmpty()) {
                    Text(pluralStringResource(R.plurals.x_errors, errors.size, errors.size))
                    for (error in errors) {
                        Text(error, modifier = Modifier.padding(start = 10.dp))
                    }
                }

                CategoryDropdownMenu(
                    categories = categories,
                    selectedCategoryId = categoryId,
                    onSelect = { categoryId = it?.id },
                    onAddCategoryClick = onAddCategoryClick,
                )

                singleSoundName?.also { name ->
                    OutlinedTextField(
                        value = name,
                        onValueChange = { singleSoundName = it },
                        label = { Text(stringResource(R.string.name)) },
                    )
                }

                if (duplicateSounds.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.import_duplicate_sounds))
                        Switch(checked = includeDuplicates, onCheckedChange = { includeDuplicates = it })
                    }
                }
            }
        },
    )
}
