package us.huseli.soundboard4.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import us.huseli.retaintheme.ui.theme.RetainTheme

@Composable
fun Soundboard4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    RetainTheme(useDarkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
