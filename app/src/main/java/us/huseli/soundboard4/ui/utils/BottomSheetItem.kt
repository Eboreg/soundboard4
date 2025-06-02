package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetItem(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
            .height(48.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        icon?.also {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) { it() }
            Spacer(modifier = Modifier.width(16.dp))
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) { text() }
    }
}

@Composable
fun BottomSheetItem(text: String, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    BottomSheetItem(
        text = { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        icon = icon?.let { { Icon(imageVector = icon, contentDescription = null) } },
        onClick = onClick,
    )
}
