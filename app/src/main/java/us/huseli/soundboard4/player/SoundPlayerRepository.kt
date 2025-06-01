package us.huseli.soundboard4.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import us.huseli.retaintheme.extensions.clone
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.data.repository.SoundRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayerRepository @Inject constructor(
    private val soundRepository: SoundRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val _soundPlayers = mutableMapOf<String, SoundPlayer>()

    fun getSoundPlayer(sound: Sound) = _soundPlayers.getOrPut(sound.uri) {
        SoundPlayer(sound, coroutineScope).also { player ->
            player.addListener(object : SoundPlayer.Listener {
                override fun onDurationChanged(durationMs: Long) {
                    coroutineScope.launch(Dispatchers.IO) { soundRepository.updateDuration(sound.id, durationMs) }
                }

                override fun onPlaybackEnded() {
                    coroutineScope.launch(Dispatchers.IO) { soundRepository.increasePlayCount(sound.id) }
                }
            })

            coroutineScope.launch(Dispatchers.IO) {
                soundRepository.flow(sound.id).filterNotNull().map { it.volume }.distinctUntilChanged().collect {
                    withContext(Dispatchers.Main) { player.setVolume(it) }
                }
            }
        }
    }

    fun stopAllPlayers() {
        for (player in _soundPlayers.values.clone()) player.stop()
    }
}
