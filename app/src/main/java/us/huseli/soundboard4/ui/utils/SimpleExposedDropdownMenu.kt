package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleExposedDropdownMenu(
    values: Collection<T>,
    initialValue: T?,
    onSelect: (T?) -> Unit,
    showEmptyItem: Boolean = false,
    item: @Composable (T?) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var currentValue by remember(initialValue) { mutableStateOf(initialValue) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        DropdownMenuItem(
            text = { item(currentValue) },
            onClick = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isExpanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            if (showEmptyItem) DropdownMenuItem(
                text = { item(null) },
                onClick = {
                    onSelect(null)
                    isExpanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                modifier = Modifier.background(
                    if (currentValue == null) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                )
            )
            for (value in values) {
                DropdownMenuItem(
                    text = { item(value) },
                    onClick = {
                        onSelect(value)
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.background(
                        if (value == currentValue) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                    )
                )
            }
        }
    }
}
