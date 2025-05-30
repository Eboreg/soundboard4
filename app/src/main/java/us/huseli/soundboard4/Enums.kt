package us.huseli.soundboard4

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Stop
import androidx.compose.ui.graphics.vector.ImageVector

enum class RepressMode(val icon: ImageVector, val resId: Int) {
    STOP(Icons.Sharp.Stop, R.string.stop),
    RESTART(Icons.Sharp.PlayArrow, R.string.restart),
    OVERLAP(Icons.Sharp.ContentCopy, R.string.overlap),
    PAUSE(Icons.Sharp.Pause, R.string.pause),
}

enum class SoundSortingKey(val resId: Int) {
    NAME(R.string.name),
    LENGTH(R.string.length),
    CREATION_TIME(R.string.creation_time),
    PLAY_COUNT(R.string.play_count),
}
