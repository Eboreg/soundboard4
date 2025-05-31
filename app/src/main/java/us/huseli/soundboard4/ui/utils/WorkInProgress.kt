package us.huseli.soundboard4.ui.utils

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import kotlin.coroutines.CoroutineContext

class WorkInProgressState(activeWorkers: Int = 0) {
    private var activeWorkers by mutableIntStateOf(activeWorkers)

    val statusRows = mutableStateListOf<String>()

    val isActive: Boolean
        get() = activeWorkers > 0

    fun addStatusRow(row: String) {
        if (statusRows.firstOrNull() != row) {
            statusRows.add(0, row)
        }
    }

    suspend fun <T> run(context: CoroutineContext? = null, statusRow: String? = null, block: suspend () -> T): T {
        onStart()
        statusRow?.also(this::addStatusRow)
        return try {
            context?.let { withContext(it) { block() } } ?: block()
        } finally {
            onStop()
        }
    }

    private fun onStart() {
        activeWorkers++
    }

    private fun onStop() {
        if (activeWorkers > 0) activeWorkers--
        if (activeWorkers == 0) statusRows.clear()
    }

    companion object {
        val Saver: Saver<WorkInProgressState, *> = listSaver(
            save = { listOf(it.activeWorkers) },
            restore = { WorkInProgressState(it[0]) },
        )
    }
}

@Composable
fun rememberWorkInProgressState(activeWorkers: Int = 0): WorkInProgressState {
    return rememberSaveable(saver = WorkInProgressState.Saver) {
        WorkInProgressState(activeWorkers)
    }
}

@Composable
fun WorkInProgressOverlay(wipState: WorkInProgressState) {
    if (wipState.isActive) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .zIndex(100f)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .padding(top = 20.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    for (statusRow in wipState.statusRows) {
                        Text(statusRow, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WorkInProgressOverlayPreview() {
    val state = rememberWorkInProgressState(1)

    LaunchedEffect(Unit) {
        for (idx in 1..20) {
            state.addStatusRow("neger $idx")
            delay(1000)
        }
    }

    Soundboard4Theme {
        WorkInProgressOverlay(state)
    }
}
