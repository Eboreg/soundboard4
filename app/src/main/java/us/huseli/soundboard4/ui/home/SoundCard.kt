package us.huseli.soundboard4.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Block
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.PauseCircle
import androidx.compose.material.icons.sharp.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import us.huseli.soundboard4.RepressMode
import us.huseli.soundboard4.darkenOrBrighten
import us.huseli.soundboard4.player.SoundPlayer
import us.huseli.soundboard4.rememberContentColorFor
import us.huseli.soundboard4.toShortString
import us.huseli.soundboard4.ui.states.SoundCardUiState
import us.huseli.soundboard4.ui.theme.Soundboard4Theme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun SoundCard(
    uiState: SoundCardUiState,
    modifier: Modifier = Modifier,
    isScrollInProgress: Boolean = false,
    onSelect: () -> Unit = {},
    onDeselect: () -> Unit = {},
    onSelectUntil: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.player.playbackState, isScrollInProgress) {
        if (uiState.player.playbackState == SoundPlayer.PlaybackState.CREATED && !isScrollInProgress) {
            withContext(Dispatchers.Main) { uiState.player.initialize(context) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            uiState.player.releaseWhenFinished()
        }
    }

    SoundCardImpl(
        uiState = uiState,
        modifier = modifier,
        progress = uiState.player.progress,
        playbackState = uiState.player.playbackState,
        duration = uiState.player.duration,
        onClick = {
            if (uiState.isSelectEnabled) {
                if (uiState.isSelected) onDeselect()
                else onSelect()
            } else {
                when (uiState.player.playbackState) {
                    SoundPlayer.PlaybackState.PLAYING -> when (uiState.repressMode) {
                        RepressMode.OVERLAP -> uiState.player.playParallel(context)
                        RepressMode.PAUSE -> uiState.player.pause()
                        RepressMode.RESTART -> uiState.player.restart()
                        RepressMode.STOP -> uiState.player.stop()
                    }
                    else -> uiState.player.play()
                }
            }
        },
        onLongClick = onSelectUntil,
    )
}

@Composable
fun SoundCardImpl(
    uiState: SoundCardUiState,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    playbackState: SoundPlayer.PlaybackState = SoundPlayer.PlaybackState.STOPPED,
    duration: Duration = uiState.duration,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    var borderAlpha by remember { mutableFloatStateOf(0f) }

    Card(
        colors = CardDefaults.cardColors(containerColor = uiState.backgroundColor),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = borderAlpha)),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .aspectRatio(4f / 3f)
            .pointerInput(uiState.isSelectEnabled, uiState.repressMode, playbackState, uiState.isSelected) {
                awaitEachGesture {
                    awaitFirstDown()

                    try {
                        withTimeout(100) { waitForUpOrCancellation() }?.also { onClick() }
                    } catch (_: PointerEventTimeoutCancellationException) {
                        borderAlpha = 1f
                        try {
                            withTimeout(400) { waitForUpOrCancellation() }?.also { onClick() }
                        } catch (_: PointerEventTimeoutCancellationException) {
                            onLongClick()
                        }
                    } finally {
                        borderAlpha = 0f
                    }
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = {
                    if (
                        listOf(
                            SoundPlayer.PlaybackState.PLAYING,
                            SoundPlayer.PlaybackState.PAUSED,
                        ).contains(playbackState)
                    ) progress else uiState.volume
                },
                modifier = Modifier.height(4.dp).fillMaxWidth().align(Alignment.BottomCenter),
                trackColor = uiState.backgroundColor,
                color = uiState.backgroundColor.darkenOrBrighten(),
                drawStopIndicator = {},
            )

            if (uiState.isSelected) SoundCardIcon(Icons.Sharp.Check, Color(0x60B2EBF2))
            else when (playbackState) {
                SoundPlayer.PlaybackState.ERROR -> SoundCardIcon(Icons.Sharp.Block)
                SoundPlayer.PlaybackState.PAUSED -> SoundCardIcon(Icons.Sharp.PauseCircle)
                SoundPlayer.PlaybackState.PLAYING -> SoundCardIcon(Icons.Sharp.PlayCircle)
                else -> {}
            }

            if (uiState.playCount > 0) {
                Card(
                    modifier = Modifier.align(Alignment.TopStart),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                ) {
                    Text(
                        text = uiState.playCount.toString(),
                        color = rememberContentColorFor(Color.DarkGray),
                        fontSize = uiState.durationFontSize,
                        lineHeight = uiState.durationFontSize,
                        modifier = Modifier.padding(1.dp),
                    )
                }
            }

            Card(
                modifier = Modifier.align(Alignment.TopEnd),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(bottomStart = 4.dp),
            ) {
                Text(
                    text = duration.toShortString(),
                    color = rememberContentColorFor(Color.DarkGray),
                    fontSize = uiState.durationFontSize,
                    lineHeight = uiState.durationFontSize,
                    modifier = Modifier.padding(1.dp),
                )
            }

            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.name,
                    textAlign = TextAlign.Center,
                    color = rememberContentColorFor(uiState.backgroundColor),
                    lineHeight = uiState.nameFontSize,
                    fontSize = uiState.nameFontSize,
                )
            }
        }
    }
}

@Composable
private fun SoundCardIcon(imageVector: ImageVector, backgroundColor: Color = Color.Transparent) {
    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(5.dp),
            tint = LocalContentColor.current.copy(alpha = 0.5f),
        )
    }
}

@Preview
@Composable
private fun SoundCardPreview() {
    Soundboard4Theme {
        SoundCardImpl(
            uiState = SoundCardUiState(
                duration = 2.5.seconds,
                name = "sound 1",
                backgroundColor = Color.Red,
                playCount = 10,
            ),
            duration = 2.5.seconds,
        )
    }
}
