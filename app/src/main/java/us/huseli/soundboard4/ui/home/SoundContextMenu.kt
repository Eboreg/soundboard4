package us.huseli.soundboard4.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.player.SoundPlayer
import us.huseli.soundboard4.ui.states.SoundCardUiState
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import us.huseli.soundboard4.ui.utils.BottomSheetItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundContextMenu(
    uiState: SoundCardUiState,
    onDismiss: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSelectClick: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
    ) {
        BottomSheetItem(uiState.name)

        HorizontalDivider(modifier = Modifier.Companion.padding(10.dp))

        BottomSheetItem(stringResource(R.string.edit), Icons.Sharp.Edit) {
            onEditClick()
            onDismiss()
        }
        BottomSheetItem(stringResource(R.string.delete), Icons.Sharp.Delete) {
            onDeleteClick()
            onDismiss()
        }
        BottomSheetItem(stringResource(R.string.select), Icons.Sharp.Check) {
            onSelectClick()
            onDismiss()
        }
        if (uiState.player.playbackState == SoundPlayer.PlaybackState.PLAYING) {
            BottomSheetItem(stringResource(R.string.stop), Icons.Sharp.Stop) {
                uiState.player.stop()
                onDismiss()
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SoundContextMenuPreview() {
    Soundboard4Theme {
        SoundContextMenu(uiState = SoundCardUiState(name = "hej hej conny ray"))
    }
}
