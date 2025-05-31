package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun <T> SimpleDropdownMenu(
    modifier: Modifier = Modifier,
    items: List<T> = emptyList(),
    selectedItem: T? = null,
    button: @Composable (isExpanded: Boolean, onClick: () -> Unit) -> Unit = { _, _ -> },
    itemText: @Composable (T) -> Unit = {},
    itemLeadingIcon: (@Composable (T) -> Unit)? = null,
    onItemClick: (T) -> Unit = {},
    isItemEnabled: (T) -> Boolean = { true },
    header: (@Composable () -> Unit)? = null,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box {
        button(isExpanded, { isExpanded = !isExpanded })

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = MaterialTheme.shapes.extraSmall,
            modifier = modifier,
        ) {
            header?.invoke()

            for (item in items) {
                DropdownMenuItem(
                    text = { itemText(item) },
                    onClick = {
                        onItemClick(item)
                        isExpanded = false
                    },
                    leadingIcon = itemLeadingIcon?.let { { it(item) } },
                    enabled = isItemEnabled(item),
                    modifier = Modifier.background(
                        if (item == selectedItem) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                    ),
                )
            }
        }
    }
}
