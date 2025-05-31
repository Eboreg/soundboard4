package us.huseli.soundboard4.ui.settings

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.huseli.soundboard4.R
import us.huseli.soundboard4.ui.states.SettingsUiState
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import us.huseli.soundboard4.ui.topbar.TopBar
import us.huseli.soundboard4.ui.utils.CategoryDropdownMenu
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import us.huseli.soundboard4.ui.utils.rememberWorkInProgressState

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    wipState: WorkInProgressState = rememberWorkInProgressState(),
    selectedCategoryId: String? = null,
    onDismiss: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autoImportDirectoryLauncher =
        rememberLauncherForActivityResult(viewModel.AutoImportDirectoryContract()) { uri ->
            if (uri != null) viewModel.setAutoImportDirectory(uri)
        }

    LaunchedEffect(selectedCategoryId) {
        selectedCategoryId?.also(viewModel::setAutoImportCategoryId)
    }

    SettingsScreenImpl(
        modifier = modifier,
        uiState = uiState,
        onDismiss = onDismiss,
        onAutoImportChange = viewModel::setAutoImport,
        onAutoImportDirectoryClick = {
            autoImportDirectoryLauncher.launch(uiState.autoImportDirectory)
        },
        onAutoImportCategoryChange = viewModel::setAutoImportCategoryId,
        onConvertToWavChange = viewModel::setConvertToWav,
        onConvertExistingFilesToWavClick = {
            scope.launch {
                wipState.run(Dispatchers.IO) { viewModel.convertExistingFilesToWav(context, wipState) }
            }
        },
        onAddCategoryClick = onAddCategoryClick,
    )
}

@Composable
private fun SettingsScreenImpl(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onAutoImportChange: (Boolean) -> Unit = {},
    onAutoImportDirectoryClick: () -> Unit = {},
    onAutoImportCategoryChange: (String) -> Unit = {},
    onConvertToWavChange: (Boolean) -> Unit = {},
    onConvertExistingFilesToWavClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
) {
    var showConvertToWavDialog by remember { mutableStateOf(false) }

    if (showConvertToWavDialog) {
        ConvertToWavDialog(
            onConfirm = {
                showConvertToWavDialog = false
                onConvertExistingFilesToWavClick()
            },
            onDismiss = { showConvertToWavDialog = false },
        )
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(horizontal = 10.dp).fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge)

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Convert sounds", style = MaterialTheme.typography.headlineSmall)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Convert sounds to WAV (good for latency)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.convertToWav,
                        onCheckedChange = {
                            if (it) showConvertToWavDialog = true
                            onConvertToWavChange(it)
                        },
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.auto_import_sounds), style = MaterialTheme.typography.headlineSmall)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.enable_auto_import), modifier = Modifier.weight(1f))
                    Switch(checked = uiState.autoImport, onCheckedChange = onAutoImportChange)
                }

                if (uiState.autoImport) {
                    Text(stringResource(R.string.auto_import_from_directory))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.autoImportDirectory?.path ?: stringResource(R.string.not_set_parenthesis),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                                .padding(8.dp)
                        )
                        OutlinedButton(
                            onClick = onAutoImportDirectoryClick,
                            shape = MaterialTheme.shapes.extraSmall,
                            content = { Text(stringResource(R.string.select)) },
                        )
                    }

                    Text(stringResource(R.string.auto_import_to_category))
                    CategoryDropdownMenu(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.autoImportCategory?.id,
                        onSelect = { if (it != null) onAutoImportCategoryChange(it.id) },
                        emptyItemText = stringResource(R.string.not_set_parenthesis),
                        onAddCategoryClick = onAddCategoryClick,
                    )
                    Text(
                        stringResource(R.string.if_not_set_the_first_category_will_be_used),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Button(
            onClick = onDismiss,
            shape = MaterialTheme.shapes.extraSmall,
            content = { Text(stringResource(R.string.close)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ConvertToWavDialog(onDismiss: () -> Unit = {}, onConfirm: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.no_thank_you)) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.yes_please)) } },
        title = { Text(stringResource(R.string.convert_sounds_to_wav)) },
        text = { Text(stringResource(R.string.convert_all_existing_non_wav_files)) },
        shape = MaterialTheme.shapes.small,
    )
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    Soundboard4Theme {
        Scaffold(
            topBar = { TopBar() },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            SettingsScreenImpl(
                SettingsUiState(autoImport = true),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
