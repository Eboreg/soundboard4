package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.RepressMode
import us.huseli.soundboard4.ui.utils.SimpleDropdownMenu

@Composable
fun RepressModeMenu(repressMode: RepressMode, onChange: (RepressMode) -> Unit) {
    SimpleDropdownMenu(
        items = RepressMode.entries,
        selectedItem = repressMode,
        modifier = Modifier.widthIn(min = 200.dp),
        button = { isExpanded, onClick ->
            IconButton(onClick = onClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(repressMode.icon, stringResource(R.string.repress_mode))
                    Icon(if (isExpanded) Icons.Sharp.ArrowDropUp else Icons.Sharp.ArrowDropDown, null)
                }
            }
        },
        itemText = { mode -> Text(stringResource(mode.resId)) },
        itemLeadingIcon = { mode -> Icon(mode.icon, null) },
        onItemClick = onChange,
        header = {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.repress_mode)) },
                onClick = {},
                enabled = false,
            )
            HorizontalDivider()
        },
    )
}
