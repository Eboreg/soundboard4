package us.huseli.soundboard4.ui

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import us.huseli.soundboard4.ui.dialogs.WelcomeDialog
import us.huseli.soundboard4.ui.dialogs.category.CategoryDeleteDialog
import us.huseli.soundboard4.ui.dialogs.category.CategoryEditDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundAddDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundDeleteDialog
import us.huseli.soundboard4.ui.dialogs.sound.SoundEditDialog
import us.huseli.soundboard4.ui.home.HomeScreen
import us.huseli.soundboard4.ui.settings.SettingsScreen
import us.huseli.soundboard4.ui.utils.WorkInProgressOverlay
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Serializable
object HomeDestination

@Serializable
data class CategoryEditDestination(val categoryId: String? = null)

@Serializable
object SoundAddDestination

@Serializable
data class SoundEditDestination(val soundId: String? = null)

@Serializable
data class SoundDeleteDestination(val soundId: String? = null)

@Serializable
data class CategoryDeleteDestination(val categoryId: String)

@Serializable
object SettingsDestination

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: AppViewModel = hiltViewModel()) {
    val configuration = LocalConfiguration.current
    val wipState = rememberWorkInProgressState()
    val navController = rememberNavController()
    val isFirstRun by viewModel.isFirstRun.collectAsStateWithLifecycle()

    LaunchedEffect(configuration.orientation) { viewModel.setOrientation(configuration.orientation) }
    LaunchedEffect(configuration.screenWidthDp) { viewModel.setScreenWidthDp(configuration.screenWidthDp) }

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
    ) {
        composable<HomeDestination> {
            HomeScreen(
                onEditCategoryClick = { navController.navigate(route = CategoryEditDestination(it)) },
                onDeleteCategoryClick = { navController.navigate(route = CategoryDeleteDestination(it)) },
                onEditSoundClick = { navController.navigate(route = SoundEditDestination(it)) },
                onDeleteSoundClick = { navController.navigate(route = SoundDeleteDestination(it)) },
                wipState = wipState,
                onDeleteSelectedSoundsClick = { navController.navigate(route = SoundDeleteDestination()) },
                onEditSelectedSoundsClick = { navController.navigate(route = SoundEditDestination()) },
                onAddCategoryClick = { navController.navigate(route = CategoryEditDestination()) },
                onSettingsClick = { navController.navigate(route = SettingsDestination) },
                onAddSoundsResult = { navController.navigate(route = SoundAddDestination) },
            )
        }

        composable<SettingsDestination> { backStackEntry ->
            SettingsScreen(
                onDismiss = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate(route = CategoryEditDestination()) },
                wipState = wipState,
                selectedCategoryId = getSelectedCategoryId(backStackEntry.savedStateHandle),
            )
        }

        dialog<CategoryEditDestination> { backStackEntry ->
            CategoryEditDialog(
                onDismiss = { navController.popBackStack() },
                onCreated = { category ->
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("selectedCategoryId", category.id)
                },
                wipState = wipState,
            )
        }
        dialog<SoundAddDestination> { backStackEntry ->
            SoundAddDialog(
                onDismiss = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate(route = CategoryEditDestination()) },
                wipState = wipState,
                selectedCategoryId = getSelectedCategoryId(backStackEntry.savedStateHandle),
            )
        }
        dialog<CategoryDeleteDestination> {
            CategoryDeleteDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
        }
        dialog<SoundDeleteDestination> {
            SoundDeleteDialog(onDismiss = { navController.popBackStack() }, wipState = wipState)
        }
        dialog<SoundEditDestination> { backStackEntry ->
            SoundEditDialog(
                onDismiss = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate(route = CategoryEditDestination()) },
                wipState = wipState,
                selectedCategoryId = getSelectedCategoryId(backStackEntry.savedStateHandle),
            )
        }
    }

    if (wipState.isActive) WorkInProgressOverlay(wipState)

    if (isFirstRun) WelcomeDialog(onDismiss = { viewModel.setIsFirstRun(false) })
}

@Composable
fun getSelectedCategoryId(savedStateHandle: SavedStateHandle): String? {
    val selectedCategoryId by savedStateHandle
        .getStateFlow<String?>("selectedCategoryId", null)
        .collectAsStateWithLifecycle()

    return selectedCategoryId
}
