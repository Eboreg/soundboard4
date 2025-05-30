package us.huseli.soundboard4.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import us.huseli.retaintheme.compose.SnackbarHosts
import us.huseli.soundboard4.ui.dialogs.category.CategoryDeleteDialog
import us.huseli.soundboard4.ui.dialogs.category.CategoryEditDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundAddDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundDeleteDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundEditDialog
import us.huseli.soundboard4.ui.home.HomeScreen
import us.huseli.soundboard4.ui.settings.SettingsScreen
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import us.huseli.soundboard4.ui.topbar.SelectTopBar
import us.huseli.soundboard4.ui.topbar.TopBar
import us.huseli.soundboard4.ui.utils.WorkInProgressOverlay
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Serializable
object HomeDestination

@Serializable
data class CategoryEditDestination(val categoryId: String? = null)

@Serializable
object SoundAddDestination

@Serializable
object SoundEditDestination

@Serializable
object SoundDeleteDestination

@Serializable
data class CategoryDeleteDestination(val categoryId: String)

@Serializable
object SettingsDestination

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: AppViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val wipState = rememberWorkInProgressState()
    val navController = rememberNavController()
    val repressMode by viewModel.repressMode.collectAsStateWithLifecycle()
    val addSoundsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        scope.launch {
            if (wipState.run(Dispatchers.IO) { viewModel.importSoundsFromUris(uris) }) {
                navController.navigate(route = SoundAddDestination)
            }
        }
    }
    val isSelectEnabled by viewModel.isSelectEnabled.collectAsStateWithLifecycle()
    val selectedSoundCount by viewModel.selectedSoundCount.collectAsStateWithLifecycle()
    val totalSoundCount by viewModel.totalSoundCount.collectAsStateWithLifecycle()
    val canZoomIn by viewModel.canZoomIn.collectAsStateWithLifecycle()
    val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()

    LaunchedEffect(configuration.orientation) { viewModel.setOrientation(configuration.orientation) }
    LaunchedEffect(configuration.screenWidthDp) { viewModel.setScreenWidthDp(configuration.screenWidthDp) }

    Soundboard4Theme {
        Scaffold(
            snackbarHost = { SnackbarHosts() },
            topBar = {
                if (isSelectEnabled) SelectTopBar(
                    selectedSoundCount = selectedSoundCount,
                    totalSoundCount = totalSoundCount,
                    onDisableSelectClick = viewModel::deselectAllSounds,
                    onSelectAllClick = viewModel::selectAllSounds,
                    onDeleteClick = { navController.navigate(route = SoundDeleteDestination) },
                    onEditClick = { navController.navigate(route = SoundEditDestination) },
                )
                else TopBar(
                    repressMode = repressMode,
                    canZoomIn = canZoomIn,
                    searchTerm = searchTerm,
                    onRepressModeChange = viewModel::setRepressMode,
                    onAddCategoryClick = { navController.navigate(route = CategoryEditDestination()) },
                    onAddSoundsClick = { addSoundsLauncher.launch("audio/*") },
                    onZoomIn = { viewModel.zoomIn(context) },
                    onZoomOut = { viewModel.zoomOut(context) },
                    onSearchTermChange = viewModel::setSearchTerm,
                    onSettingsClick = { navController.navigate(route = SettingsDestination) },
                    onStopAllPlaybackClick = viewModel::stopAllPlayers,
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<HomeDestination> {
                    HomeScreen(
                        onEditCategoryClick = { navController.navigate(route = CategoryEditDestination(it)) },
                        onDeleteCategoryClick = { navController.navigate(route = CategoryDeleteDestination(it)) },
                        onZoomIn = { viewModel.zoomIn(context) },
                        onZoomOut = { viewModel.zoomOut(context) },
                    )
                }

                composable<SettingsDestination> {
                    SettingsScreen(onDismiss = { navController.navigate(route = HomeDestination) }, wipState = wipState)
                }

                dialog<CategoryEditDestination> {
                    CategoryEditDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
                }
                dialog<SoundAddDestination> {
                    SoundAddDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
                }
                dialog<CategoryDeleteDestination> {
                    CategoryDeleteDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
                }
                dialog<SoundDeleteDestination> {
                    SoundDeleteDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
                }
                dialog<SoundEditDestination> {
                    SoundEditDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
                }
            }
        }

        WorkInProgressOverlay(wipState)
    }
}
