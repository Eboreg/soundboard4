package us.huseli.soundboard4.ui.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.Redo
import androidx.compose.material.icons.automirrored.sharp.Undo
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.Block
import androidx.compose.material.icons.sharp.CreateNewFolder
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.ZoomIn
import androidx.compose.material.icons.sharp.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import us.huseli.soundboard4.R
import us.huseli.soundboard4.RepressMode
import us.huseli.soundboard4.ui.states.TopBarUiState
import us.huseli.soundboard4.ui.utils.SimpleDropdownMenu

enum class TopBarDropdownMenuItem(val text: Int, val icon: ImageVector) {
    AddSounds(R.string.add_sound_s, Icons.Sharp.Add),
    AddCategory(R.string.add_category, Icons.Sharp.CreateNewFolder),
    ZoomIn(R.string.zoom_in, Icons.Sharp.ZoomIn),
    ZoomOut(R.string.zoom_out, Icons.Sharp.ZoomOut),
    Undo(R.string.undo, Icons.AutoMirrored.Sharp.Undo),
    Redo(R.string.redo, Icons.AutoMirrored.Sharp.Redo),
    Settings(R.string.settings, Icons.Sharp.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    uiState: TopBarUiState = TopBarUiState(),
    onRepressModeChange: (RepressMode) -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onAddSoundsClick: () -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onSearchTermChange: (String?) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStopAllPlaybackClick: () -> Unit = {},
    onUndoClick: () -> Unit = {},
    onRedoClick: () -> Unit = {},
) {
    TopAppBar(
        title = {},
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth()
            ) {
                TopBarLogo(modifier = Modifier.padding(vertical = 10.dp).padding(end = 10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.searchTerm != null) {
                        SoundFilterTextField(
                            value = uiState.searchTerm,
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
                        RepressModeMenu(uiState.repressMode, onRepressModeChange)
                    }

                    SimpleDropdownMenu(
                        items = TopBarDropdownMenuItem.entries,
                        button = { isExpanded, onClick ->
                            IconButton(onClick = onClick) { Icon(Icons.Sharp.MoreVert, null) }
                        },
                        itemText = { item -> Text(stringResource(item.text)) },
                        itemLeadingIcon = { item -> Icon(item.icon, null) },
                        onItemClick = { item ->
                            when (item) {
                                TopBarDropdownMenuItem.AddSounds -> onAddSoundsClick()
                                TopBarDropdownMenuItem.AddCategory -> onAddCategoryClick()
                                TopBarDropdownMenuItem.ZoomIn -> onZoomIn()
                                TopBarDropdownMenuItem.ZoomOut -> onZoomOut()
                                TopBarDropdownMenuItem.Settings -> onSettingsClick()
                                TopBarDropdownMenuItem.Undo -> onUndoClick()
                                TopBarDropdownMenuItem.Redo -> onRedoClick()
                            }
                        },
                        isItemEnabled = { item ->
                            when (item) {
                                TopBarDropdownMenuItem.ZoomIn -> uiState.canZoomIn
                                TopBarDropdownMenuItem.Undo -> uiState.canUndo
                                TopBarDropdownMenuItem.Redo -> uiState.canRedo
                                else -> true
                            }
                        },
                        modifier = Modifier.widthIn(min = 200.dp),
                    )
                }
            }
        },
        modifier = modifier,
    )
}
