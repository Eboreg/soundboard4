package us.huseli.soundboard4.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowRight
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.EditNote
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material.icons.sharp.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.rememberContentColorFor
import us.huseli.soundboard4.ui.states.CategoryUiState
import us.huseli.soundboard4.ui.theme.Soundboard4Theme

@Composable
fun CategoryHeader(
    uiState: CategoryUiState,
    modifier: Modifier = Modifier,
    onToggleCollapseClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onMoveUpClick: () -> Unit = {},
    onMoveDownClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    val textColor = rememberContentColorFor(uiState.backgroundColor)

    Column(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(uiState.backgroundColor)
                .fillMaxWidth()
        ) {
            CompositionLocalProvider(LocalContentColor provides textColor) {
                IconButton(onClick = onToggleCollapseClick) {
                    Icon(
                        if (uiState.isCollapsed) Icons.AutoMirrored.Sharp.ArrowRight else Icons.Sharp.ArrowDropDown,
                        null,
                    )
                }
                Text(
                    uiState.name,
                    modifier = Modifier.padding(vertical = 10.dp).weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onMoveUpClick, enabled = !uiState.isFirst) {
                    Icon(Icons.Sharp.KeyboardArrowUp, stringResource(R.string.move_up))
                }
                IconButton(onClick = onMoveDownClick, enabled = !uiState.isLast) {
                    Icon(Icons.Sharp.KeyboardArrowDown, stringResource(R.string.move_down))
                }
                IconButton(onClick = onEditClick) { Icon(Icons.Sharp.EditNote, stringResource(R.string.edit_category)) }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Sharp.Delete,
                        stringResource(R.string.delete_category)
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Preview
@Composable
private fun CategoryHeaderPreview() {
    Soundboard4Theme {
        CategoryHeader(CategoryUiState(backgroundColor = Color.Red, name = "category 1"))
    }
}
