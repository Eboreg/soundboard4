package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import us.huseli.soundboard4.R
import us.huseli.soundboard4.RepressMode

@Composable
fun RepressModeMenu(repressMode: RepressMode, onChange: (RepressMode) -> Unit) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    IconButton(onClick = { isExpanded = !isExpanded }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(repressMode.icon, stringResource(R.string.repress_mode))
            Icon(if (isExpanded) Icons.Sharp.ArrowDropUp else Icons.Sharp.ArrowDropDown, null)
        }
    }

    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { isExpanded = false },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.repress_mode)) },
            onClick = {},
            enabled = false,
        )

        HorizontalDivider()

        for (mode in RepressMode.entries) {
            DropdownMenuItem(
                text = { Text(stringResource(mode.resId)) },
                onClick = {
                    onChange(mode)
                    isExpanded = false
                },
                leadingIcon = { Icon(mode.icon, null) },
                modifier = Modifier.background(
                    if (mode == repressMode) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                ),
            )
        }
    }
}
