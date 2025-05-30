package us.huseli.soundboard4.ui.dialogs

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R

@Composable
fun HallonDialog(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        playRawFile(R.raw.hallon1, context, scope)
    }

    HallonDialogImpl(
        onLugnaPuckarClick = {
            playRawFile(R.raw.hallon2, context, scope)
            onDismiss()
        },
        onHockeyklubbaClick = {
            playRawFile(R.raw.hallon3, context, scope)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

private fun playRawFile(id: Int, context: Context, scope: CoroutineScope) {
    scope.launch {
        MediaPlayer.create(context, id).apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }
}

@Composable
private fun HallonDialogImpl(
    onLugnaPuckarClick: () -> Unit = {},
    onHockeyklubbaClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onLugnaPuckarClick) { Text("Lugna puckar") } },
        confirmButton = { TextButton(onClick = onHockeyklubbaClick) { Text("Hockeyklubba") } },
        shape = MaterialTheme.shapes.small,
        text = { Text("Hur fan är det med dig, karl?") },
    )
}

@Preview
@Composable
private fun HallonDialogPreview() {
    HallonDialogImpl()
}
