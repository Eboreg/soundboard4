package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import us.huseli.soundboard4.R

@Composable
fun SoundFilterTextField(value: String, onValueChange: (String?) -> Unit, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = value,
        singleLine = true,
        onValueChange = onValueChange,
        leadingIcon = { Icon(Icons.Sharp.Search, stringResource(R.string.search_sounds)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        ),
        trailingIcon = {
            Icon(
                imageVector = Icons.Sharp.Close,
                contentDescription = stringResource(R.string.clear_search),
                modifier = Modifier.clickable {
                    onValueChange(null)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            )
        },
        modifier = modifier.focusRequester(focusRequester)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
