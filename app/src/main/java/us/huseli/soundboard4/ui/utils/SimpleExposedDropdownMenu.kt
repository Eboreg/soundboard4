package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleExposedDropdownMenu(
    values: Collection<T>,
    initialValue: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    showEmptyItem: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    item: @Composable (T?) -> Unit,
) {
    val density = LocalDensity.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var currentValue by remember(initialValue) { mutableStateOf(initialValue) }
    var labelBounds by remember { mutableStateOf<Rect?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Box(
                modifier = Modifier
                    .offset(x = 12.dp, y = (-4).dp)
                    .padding(horizontal = 4.dp)
                    .zIndex(1f)
                    .onPlaced { coords ->
                        labelBounds = coords.boundsInParent()
                    }
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium, label)
            }
        }

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = modifier.padding(top = label?.let { 4.dp } ?: 0.dp).drawWithContent {
                val bounds = labelBounds
                val horizontalPadding = with(density) { 4.dp.toPx() }

                if (bounds != null) {
                    clipRect(
                        left = bounds.left - horizontalPadding,
                        top = 0f,
                        right = bounds.right + horizontalPadding,
                        bottom = 3f,
                        clipOp = ClipOp.Difference,
                    ) { this@drawWithContent.drawContent() }
                } else this.drawContent()
            }
        ) {
            DropdownMenuItem(
                text = { item(currentValue) },
                onClick = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isExpanded) },
                contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                    .height(56.dp)
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
                    contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
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
                        contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
                        modifier = Modifier.background(
                            if (value == currentValue) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                        )
                    )
                }
            }
        }
    }
}
