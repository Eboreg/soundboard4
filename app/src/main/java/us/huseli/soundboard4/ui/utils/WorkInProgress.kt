package us.huseli.soundboard4.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WorkInProgressState(activeWorkers: Int = 0) {
    private var activeWorkers by mutableIntStateOf(activeWorkers)

    val isActive: Boolean
        get() = activeWorkers > 0

    suspend fun <T> run(context: CoroutineContext? = null, block: suspend () -> T): T {
        onStart()
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
    }

    companion object {
        val Saver: Saver<WorkInProgressState, *> = listSaver(
            save = { listOf(it.activeWorkers) },
            restore = { WorkInProgressState(it[0]) },
        )
    }
}

@Composable
fun rememberWorkInProgressState(): WorkInProgressState {
    return rememberSaveable(saver = WorkInProgressState.Saver) {
        WorkInProgressState()
    }
}

@Composable
fun WorkInProgressOverlay(wipState: WorkInProgressState) {
    if (wipState.isActive) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, usePlatformDefaultWidth = false),
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).zIndex(100f)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
