package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import us.huseli.soundboard4.ui.states.SoundAddUiState
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
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var categoryId by remember(uiState.categories, selectedCategoryId) {
        mutableStateOf(selectedCategoryId ?: uiState.categories.firstOrNull()?.id)
    }
    var singleSoundName by remember(uiState.singleSound) { mutableStateOf(uiState.singleSound?.name) }

    SoundAddDialogImpl(
        uiState = uiState,
        singleSoundName = singleSoundName,
        onDismiss = onDismiss,
        onConfirmClick = {
            scope.launch {
                categoryId?.also {
                    wipState.run(Dispatchers.IO) { viewModel.save(it, singleSoundName, wipState) }
                    onDismiss()
                }
            }
        },
        onAddCategoryClick = onAddCategoryClick,
        categoryId = categoryId,
        onCategorySelect = { categoryId = it },
        onNameChange = { singleSoundName = it },
        onIncludeDuplicatesChange = viewModel::setIncludeDuplicates,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundAddDialogImpl(
    uiState: SoundAddUiState = SoundAddUiState(),
    categoryId: String? = null,
    singleSoundName: String? = null,
    onDismiss: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onCategorySelect: (String?) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onIncludeDuplicatesChange: (Boolean) -> Unit = {},
) {
    val netSoundCount = remember(uiState.newSoundCount, uiState.duplicateSoundCount, uiState.includeDuplicates) {
        uiState.newSoundCount + if (uiState.includeDuplicates) uiState.duplicateSoundCount else 0
    }
    val confirmButtonEnabled = remember(netSoundCount, categoryId) {
        categoryId != null && netSoundCount > 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmClick, enabled = confirmButtonEnabled) {
                Text(
                    if (netSoundCount > 0) pluralStringResource(R.plurals.add_x_sounds, netSoundCount, netSoundCount)
                    else stringResource(R.string.no_sounds_to_add)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        shape = MaterialTheme.shapes.small,
        title = { Text(pluralStringResource(R.plurals.add_sound, uiState.totalSoundCount)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    pluralStringResource(
                        R.plurals.x_sounds_found,
                        uiState.totalSoundCount,
                        uiState.totalSoundCount,
                    )
                )

                if (uiState.errors.isNotEmpty()) {
                    Column {
                        Text(pluralStringResource(R.plurals.x_errors, uiState.errors.size, uiState.errors.size))
                        for (error in uiState.errors) {
                            Text(error, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.category))
                    CategoryDropdownMenu(
                        categories = uiState.categories,
                        selectedCategoryId = categoryId,
                        onSelect = { onCategorySelect(it?.id) },
                        onAddCategoryClick = onAddCategoryClick,
                    )
                }

                singleSoundName?.also { name ->
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.name)) },
                    )
                }

                if (uiState.duplicateSoundCount > 0) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            pluralStringResource(
                                R.plurals.x_sounds_already_in_database,
                                uiState.duplicateSoundCount,
                                uiState.duplicateSoundCount,
                            ) + " " + pluralStringResource(
                                R.plurals.add_anyway_question,
                                uiState.duplicateSoundCount,
                                uiState.duplicateSoundCount,
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(pluralStringResource(R.plurals.add_duplicate_sounds, uiState.duplicateSoundCount))
                            Switch(checked = uiState.includeDuplicates, onCheckedChange = onIncludeDuplicatesChange)
                        }
                    }
                }
            }
        },
    )
}
