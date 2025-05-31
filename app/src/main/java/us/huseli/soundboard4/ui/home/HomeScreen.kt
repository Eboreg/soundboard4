package us.huseli.soundboard4.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import us.huseli.soundboard4.ui.states.CategoryUiState
import us.huseli.soundboard4.ui.states.HomeUiState
import us.huseli.soundboard4.ui.states.SoundCardUiState
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import us.huseli.soundboard4.ui.topbar.TopBar
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEditCategoryClick: (String) -> Unit = {},
    onDeleteCategoryClick: (String) -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
) {
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    HomeScreenImpl(
        uiState = uiState,
        modifier = modifier,
        onSetCategoryCollapsedClick = viewModel::setCategoryCollapsed,
        onEditCategoryClick = onEditCategoryClick,
        onMoveCategoryDownClick = viewModel::moveCategoryDown,
        onMoveCategoryUpClick = viewModel::moveCategoryUp,
        onDeleteCategoryClick = onDeleteCategoryClick,
        onZoomIn = onZoomIn,
        onZoomOut = onZoomOut,
    ) { soundCardUiState, isScrollInProgress ->
        SoundCard(
            uiState = soundCardUiState,
            onSelect = { viewModel.selectSound(soundCardUiState.id) },
            onDeselect = { viewModel.deselectSound(soundCardUiState.id) },
            onSelectUntil = { viewModel.selectSoundsUntil(soundCardUiState.id) },
            isScrollInProgress = isScrollInProgress,
        )
    }
}

@Composable
private fun HomeScreenImpl(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onSetCategoryCollapsedClick: (String, Boolean) -> Unit = { _, _ -> },
    onEditCategoryClick: (String) -> Unit = {},
    onMoveCategoryDownClick: (String) -> Unit = {},
    onMoveCategoryUpClick: (String) -> Unit = {},
    onDeleteCategoryClick: (String) -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    soundCard: @Composable (SoundCardUiState, Boolean) -> Unit = { _, _ -> },
) {
    var accumZoom by remember { mutableFloatStateOf(1f) }
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(count = uiState.columnCount),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (zoom == 1f) accumZoom = 1f
                    else accumZoom *= zoom

                    if (accumZoom <= 0.5f) {
                        onZoomOut()
                        accumZoom = 1f
                    } else if (accumZoom >= 1.5f) {
                        onZoomIn()
                        accumZoom = 1f
                    }
                }
            }
    ) {
        for (categoryUiState in uiState.categoryUiStates) {
            stickyHeader(key = "category${categoryUiState.id}") {
                CategoryHeader(
                    uiState = categoryUiState,
                    onToggleCollapseClick = {
                        onSetCategoryCollapsedClick(categoryUiState.id, !categoryUiState.isCollapsed)
                    },
                    onEditClick = { onEditCategoryClick(categoryUiState.id) },
                    onMoveDownClick = { onMoveCategoryDownClick(categoryUiState.id) },
                    onMoveUpClick = { onMoveCategoryUpClick(categoryUiState.id) },
                    onDeleteClick = { onDeleteCategoryClick(categoryUiState.id) },
                )
            }
            if (!categoryUiState.isCollapsed) {
                items(categoryUiState.soundCardUiStates, key = { "sound${it.id}" }) { soundCardUiState ->
                    soundCard(soundCardUiState, lazyGridState.isScrollInProgress)
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    val uiState = HomeUiState(
        categoryUiStates = listOf(
            CategoryUiState(
                name = "category 1",
                backgroundColor = Color.Red,
                isFirst = true,
                soundCardUiStates = (1..6).map { idx ->
                    SoundCardUiState(name = "sound $idx", backgroundColor = Color.Red, duration = idx.seconds)
                },
            ),
            CategoryUiState(
                name = "category 2",
                backgroundColor = Color.Blue,
                isFirst = true,
                soundCardUiStates = (7..10).map { idx ->
                    SoundCardUiState(name = "sound $idx", backgroundColor = Color.Blue, duration = idx.seconds)
                },
            ),
        )
    )

    Soundboard4Theme {
        Scaffold(
            topBar = { TopBar() },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            HomeScreenImpl(
                uiState = uiState,
                modifier = Modifier.padding(innerPadding)
            ) { soundCardUiState, isScrollInProgress ->
                SoundCardImpl(uiState = soundCardUiState)
            }
        }
    }
}
