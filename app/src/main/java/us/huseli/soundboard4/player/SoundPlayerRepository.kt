package us.huseli.soundboard4.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        SoundPlayer(sound, coroutineScope).also {
            it.addListener(object : SoundPlayer.Listener {
                override fun onPlaybackEnded() {
                    coroutineScope.launch(Dispatchers.IO) { soundRepository.increasePlayCount(sound.id) }
                }
            })
        }
    }

    fun stopAllPlayers() {
        for (player in _soundPlayers.values.clone()) player.stop()
    }
}
