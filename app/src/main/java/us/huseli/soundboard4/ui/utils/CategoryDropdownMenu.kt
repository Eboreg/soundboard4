package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.data.database.model.Category

@Composable
fun CategoryDropdownMenu(
    categories: List<Category>,
    initialValue: Category?,
    showEmptyItem: Boolean = false,
    emptyItemText: String = "-",
    onSelect: (Category?) -> Unit,
) {
    SimpleExposedDropdownMenu(
        values = categories,
        initialValue = initialValue,
        onSelect = onSelect,
        showEmptyItem = showEmptyItem,
    ) { category ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = category?.let { Color(it.backgroundColor) } ?: Color.DarkGray,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                content = {},
                modifier = Modifier.size(24.dp),
            )
            Text(category?.name ?: emptyItemText)
        }
    }
}
