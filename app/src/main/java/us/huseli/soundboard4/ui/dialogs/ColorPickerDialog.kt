package us.huseli.soundboard4.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import us.huseli.soundboard4.R

@Composable
fun ColorPickerDialog(
    initialColor: Color = Color.White,
    title: (@Composable () -> Unit)? = null,
    onConfirm: (Color) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val controller = rememberColorPickerController()
    var color by remember(initialColor) { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        confirmButton = { TextButton(onClick = { onConfirm(color) }) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = title,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HsvColorPicker(
                    controller = controller,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    initialColor = initialColor,
                    onColorChanged = { color = it.color },
                )
                BrightnessSlider(
                    controller = controller,
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    initialColor = initialColor,
                )
                AlphaTile(
                    controller = controller,
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                        .clip(MaterialTheme.shapes.extraSmall)
                )
            }
        },
    )
}

@Preview
@Composable
private fun ColorPickerDialogPreview() {
    ColorPickerDialog()
}
