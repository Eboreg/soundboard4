package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.ui.dialogs.HallonDialog

@Composable
fun TopBarLogo(modifier: Modifier = Modifier) {
    val clickTimes = remember { mutableListOf<Long>() }
    var isHallonDialogOpen by rememberSaveable { mutableStateOf(false) }

    if (isHallonDialogOpen) {
        HallonDialog(onDismiss = { isHallonDialogOpen = false })
    }

    Image(
        painter = painterResource(R.drawable.ic_launcher_round),
        contentDescription = null,
        modifier = modifier.height(40.dp).clickable {
            val now = System.currentTimeMillis()

            clickTimes.removeIf { it + 1000 < now }
            clickTimes.add(now)
            if (clickTimes.size >= 3) {
                isHallonDialogOpen = true
                clickTimes.clear()
            }
        }
    )
}
