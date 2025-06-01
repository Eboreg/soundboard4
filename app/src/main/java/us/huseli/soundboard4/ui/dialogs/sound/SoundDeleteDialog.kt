package us.huseli.soundboard4.ui.dialogs.sound

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun SoundDeleteDialog(
    viewModel: SoundDeleteViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    onDismiss: () -> Unit
) {
    val soundCount by viewModel.soundCount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    SoundDeleteDialogImpl(
        soundCount = soundCount,
        onDismiss = onDismiss,
        onConfirm = {
            scope.launch {
                wipState.run(
                    Dispatchers.IO,
                    context.resources.getQuantityString(R.plurals.deleting_x_sounds, soundCount, soundCount),
                ) { viewModel.delete() }
                onDismiss()
            }
        },
    )
}

@Composable
private fun SoundDeleteDialogImpl(
    soundCount: Int,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        shape = MaterialTheme.shapes.small,
        title = { Text(pluralStringResource(R.plurals.delete_sound, soundCount)) },
        text = {
            Text(pluralStringResource(R.plurals.do_you_want_to_delete_the_selected_x_sounds, soundCount, soundCount))
        },
    )
}
