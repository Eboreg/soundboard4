package us.huseli.soundboard4.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SoundCardIcon(imageVector: ImageVector, backgroundColor: Color = Color.Companion.Transparent) {
    Box(modifier = Modifier.Companion.fillMaxSize().background(backgroundColor)) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.Companion.fillMaxSize().padding(5.dp),
            tint = LocalContentColor.current.copy(alpha = 0.5f),
        )
    }
}
