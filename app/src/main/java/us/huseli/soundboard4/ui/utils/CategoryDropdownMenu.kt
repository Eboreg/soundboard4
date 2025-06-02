package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Category

@Composable
fun CategoryDropdownMenu(
    categories: List<Category>,
    selectedCategoryId: String? = null,
    showEmptyItem: Boolean = false,
    emptyItemText: String = "-",
    onSelect: (Category?) -> Unit,
    onAddCategoryClick: () -> Unit,
    label: (@Composable () -> Unit)? = null,
) {
    val initialValue = remember(selectedCategoryId, categories) { categories.find { it.id == selectedCategoryId } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        SimpleExposedDropdownMenu(
            values = categories,
            label = label,
            initialValue = initialValue,
            onSelect = onSelect,
            showEmptyItem = showEmptyItem,
            modifier = Modifier.weight(1f)
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
        IconButton(onClick = onAddCategoryClick) { Icon(Icons.Sharp.AddCircle, stringResource(R.string.add_category)) }
    }
}
