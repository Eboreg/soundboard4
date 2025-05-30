package us.huseli.soundboard4.ui.dialogs.category

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun CategoryDeleteDialog(
    viewModel: CategoryDeleteViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    onDismiss: () -> Unit = {},
) {
    val category by viewModel.category.collectAsStateWithLifecycle()
    val sounds by viewModel.sounds.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    category?.also {
        CategoryDeleteDialogImpl(
            category = it,
            sounds = sounds,
            onDismiss = onDismiss,
            onConfirm = {
                scope.launch {
                    wipState.run(Dispatchers.IO) { viewModel.delete() }
                    onDismiss()
                }
            },
        )
    }
}

@Composable
private fun CategoryDeleteDialogImpl(
    category: Category,
    sounds: List<Sound>,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        title = { Text(stringResource(R.string.delete_category)) },
        text = {
            Text(
                if (sounds.isNotEmpty()) pluralStringResource(
                    R.plurals.do_you_want_to_delete_x_and_its_x_sounds,
                    sounds.size,
                    category.name,
                    sounds.size,
                )
                else stringResource(R.string.do_you_want_to_delete_x, category.name)
            )
        },
    )
}
