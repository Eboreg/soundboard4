package us.huseli.soundboard4.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.huseli.retaintheme.extensions.clone
import us.huseli.retaintheme.snackbar.SnackbarEngine
import us.huseli.soundboard4.data.database.model.Sound
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SoundPlayer(
    private val uri: String,
    volume: Float = 1f,
    duration: Duration = Duration.ZERO,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : Player.Listener {
    constructor(sound: Sound, scope: CoroutineScope = CoroutineScope(Dispatchers.Main)) :
        this(uri = sound.uri, volume = sound.volume, duration = sound.duration, scope = scope)

    enum class PlaybackState { CREATED, STOPPED, PLAYING, PAUSED, ERROR }

    interface Listener {
        fun onDurationChanged(durationMs: Long) {}
        fun onPlaybackEnded() {}
    }

    private val _createPlayerLock = Mutex()
    private var _durationMs by mutableLongStateOf(duration.inWholeMilliseconds)
    private val _listeners = mutableListOf<Listener>()
    private val _oldPlayers = mutableListOf<ExoPlayer>()
    private var _playbackPosition by mutableLongStateOf(0)
    private var _playbackPositionJob: Job? = null
    private var _playbackState by mutableStateOf(PlaybackState.CREATED)
    private var _player: ExoPlayer? = null
    private var _volume: Float = volume

    val duration: Duration
        get() = _durationMs.milliseconds

    val playbackState: PlaybackState
        get() = _playbackState

    val progress: Float
        get() = if (_durationMs > 0) _playbackPosition.toFloat() / _durationMs else 0f

    fun addListener(listener: Listener) {
        if (!_listeners.contains(listener)) _listeners.add(listener)
    }

    fun initialize(context: Context) {
        if (_player == null) {
            scope.launch(Dispatchers.Main) {
                _player = _createPlayerLock.withLock { createPlayer(context) }
                _playbackState = PlaybackState.STOPPED
            }
        }

        if (_playbackPositionJob == null || _playbackPositionJob?.isCancelled == true) {
            _playbackPositionJob = scope.launch(Dispatchers.Main) {
                snapshotFlow { _playbackState }.map { it == PlaybackState.PLAYING }
                    .distinctUntilChanged()
                    .collectLatest { isPlaying ->
                        while (isPlaying) {
                            _player?.currentPosition?.also { _playbackPosition = it }
                            delay(100)
                        }
                    }
            }
        }
    }

    fun pause() {
        logPlayerStatus("pause")
        _player?.pause()
        _oldPlayers.clone().forEach { it.stop() }
    }

    fun play() {
        logPlayerStatus("play")
        withPlayer {
            if (it.playbackState == Player.STATE_IDLE) it.prepare()
            if (_playbackState != PlaybackState.PAUSED && it.currentPosition > 0) it.seekTo(0)
            it.play()
        }
    }

    fun playParallel(context: Context) {
        logPlayerStatus("playParallel")

        withPlayer {
            if (it.isPlaying) {
                val oldPlayer = it

                _oldPlayers.add(oldPlayer)
                oldPlayer.removeListener(this)
                oldPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                            if (playbackState == Player.STATE_ENDED) {
                                _listeners.forEach { it.onPlaybackEnded() }
                            }
                            oldPlayer.release()
                            _oldPlayers.remove(oldPlayer)
                        }
                    }
                })
                _player = createPlayer(context).apply {
                    playWhenReady = true
                    prepare()
                }
            } else play()
        }
    }

    fun releaseWhenFinished() {
        _player?.also {
            if (it.isPlaying == false) release()
            else it.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    release()
                }
            })
        }
    }

    fun restart() {
        logPlayerStatus("restart")
        withPlayer {
            it.seekTo(0)
            if (!it.isPlaying) it.play()
        }
    }

    fun setVolume(value: Float) {
        if (value != _volume) {
            _volume = value
            _player?.volume = value
            _oldPlayers.forEach { it.volume = value }
        }
    }

    fun stop() {
        logPlayerStatus("stop")
        _player?.stop()
        _oldPlayers.clone().forEach { it.stop() }
    }

    @OptIn(UnstableApi::class)
    private fun createPlayer(context: Context): ExoPlayer {
        if (DEBUG) Log.i("SoundPlayer", "createPlayer, uri=$uri, hash=${System.identityHashCode(this)}")
        return ExoPlayer.Builder(context)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build(), false)
            .setPriority(C.PRIORITY_MAX)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                volume = _volume
                addListener(this@SoundPlayer)
            }
    }

    @OptIn(UnstableApi::class)
    private fun logPlayerStatus(caller: String = "unknown") {
        if (DEBUG) {
            _player?.also {
                val state = playbackStateToString(it.playbackState)

                Log.i(
                    "SoundPlayer",
                    "playbackState=$state, isPlaying=${it.isPlaying}, currentPosition=${it.currentPosition}, " +
                        "isLoading=${it.isLoading}, isReleased=${it.isReleased}, caller=$caller"
                )
            }
        }
    }

    private fun playbackStateToString(playbackState: Int) = when (playbackState) {
        Player.STATE_BUFFERING -> "STATE_BUFFERING"
        Player.STATE_ENDED -> "STATE_ENDED"
        Player.STATE_IDLE -> "STATE_IDLE"
        Player.STATE_READY -> "STATE_READY"
        else -> playbackState.toString()
    }

    private fun release() {
        if (DEBUG) Log.i("SoundPlayer", "release, uri=$uri, hash=${System.identityHashCode(this)}")
        _playbackPositionJob?.cancel()
        _playbackPositionJob = null
        _player?.also {
            it.removeListener(this)
            it.release()
        }
        _player = null
        _oldPlayers.clone().forEach { it.release() }
        _oldPlayers.clear()
        _playbackState = PlaybackState.CREATED
    }

    private fun setPlaybackState(value: PlaybackState, caller: String = "unknown") {
        if (DEBUG) Log.i("SoundPlayer", "setPlaybackState($value), caller: $caller")
        _playbackState = value
    }

    private fun withPlayer(block: (ExoPlayer) -> Unit) {
        _player?.also(block) ?: run {
            if (_createPlayerLock.isLocked) scope.launch(Dispatchers.Main) {
                _createPlayerLock.withLock { _player }?.also(block)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _player?.also {
            setPlaybackState(
                when {
                    isPlaying -> PlaybackState.PLAYING
                    it.playbackState == Player.STATE_READY && !it.playWhenReady -> PlaybackState.PAUSED
                    else -> PlaybackState.STOPPED
                },
                "onIsPlayingChanged",
            )
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) _listeners.forEach { it.onPlaybackEnded() }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        if (error != null) {
            SnackbarEngine.addError(error.toString())
            setPlaybackState(PlaybackState.ERROR, "onPlayerErrorChanged")
        } else if (_playbackState == PlaybackState.ERROR) {
            setPlaybackState(PlaybackState.STOPPED, "onPlayerErrorChanged")
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            _player?.also { player ->
                if (player.duration > 0 && player.duration != _durationMs) {
                    _durationMs = player.duration
                    _listeners.forEach { it.onDurationChanged(_durationMs) }
                }
            }
        }
    }

    companion object {
        const val DEBUG = false
    }
}
