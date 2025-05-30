package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.Block
import androidx.compose.material.icons.sharp.CreateNewFolder
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.ZoomIn
import androidx.compose.material.icons.sharp.ZoomOut
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.RepressMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    repressMode: RepressMode = RepressMode.STOP,
    canZoomIn: Boolean = true,
    searchTerm: String? = null,
    onRepressModeChange: (RepressMode) -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onAddSoundsClick: () -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onSearchTermChange: (String?) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStopAllPlaybackClick: () -> Unit = {},
) {
    var isDropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        title = {},
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopBarLogo(modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchTerm != null) {
                        SoundFilterTextField(
                            value = searchTerm,
                            onValueChange = onSearchTermChange,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        IconButton(onClick = { onSearchTermChange("") }) {
                            Icon(Icons.Sharp.Search, stringResource(R.string.search_for_sounds))
                        }
                        IconButton(onClick = onStopAllPlaybackClick) {
                            Icon(Icons.Sharp.Block, stringResource(R.string.stop_all_playback))
                        }
                        RepressModeMenu(repressMode, onRepressModeChange)
                    }

                    IconButton(onClick = { isDropdownMenuExpanded = !isDropdownMenuExpanded }) {
                        Icon(Icons.Sharp.MoreVert, null)
                    }
                    DropdownMenu(
                        expanded = isDropdownMenuExpanded,
                        onDismissRequest = { isDropdownMenuExpanded = false },
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_sound_s)) },
                            onClick = {
                                isDropdownMenuExpanded = false
                                onAddSoundsClick()
                            },
                            leadingIcon = { Icon(Icons.Sharp.Add, null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_category)) },
                            onClick = {
                                isDropdownMenuExpanded = false
                                onAddCategoryClick()
                            },
                            leadingIcon = { Icon(Icons.Sharp.CreateNewFolder, null) },
                        )
                        if (canZoomIn) DropdownMenuItem(
                            text = { Text(stringResource(R.string.zoom_in)) },
                            onClick = {
                                isDropdownMenuExpanded = false
                                onZoomIn()
                            },
                            leadingIcon = { Icon(Icons.Sharp.ZoomIn, null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.zoom_out)) },
                            onClick = {
                                isDropdownMenuExpanded = false
                                onZoomOut()
                            },
                            leadingIcon = { Icon(Icons.Sharp.ZoomOut, null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                isDropdownMenuExpanded = false
                                onSettingsClick()
                            },
                            leadingIcon = { Icon(Icons.Sharp.Settings, null) },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
