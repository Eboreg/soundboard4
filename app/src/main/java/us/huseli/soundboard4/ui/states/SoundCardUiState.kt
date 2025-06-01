package us.huseli.soundboard4.ui.states

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import us.huseli.soundboard4.Constants.SOUND_DURATION_FONT_SIZE
import us.huseli.soundboard4.Constants.SOUND_NAME_FONT_SIZE
import us.huseli.soundboard4.RepressMode
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.player.SoundPlayer
import java.util.UUID
import kotlin.time.Duration

@Immutable
data class SoundCardUiState(
    val id: String = UUID.randomUUID().toString(),
    val uri: String = "",
    val volume: Float = 1f,
    val duration: Duration = Duration.Companion.ZERO,
    val name: String = "",
    val backgroundColor: Color = Color.Companion.White,
    val repressMode: RepressMode = RepressMode.STOP,
    val isSelectEnabled: Boolean = false,
    val isSelected: Boolean = false,
    val nameFontSize: TextUnit = SOUND_NAME_FONT_SIZE.sp,
    val durationFontSize: TextUnit = SOUND_DURATION_FONT_SIZE.sp,
    val playCount: Int = 0,
    val player: SoundPlayer = SoundPlayer(
        uri = uri,
        volume = volume,
        duration = duration,
    ),
) {
    constructor(
        sound: Sound,
        player: SoundPlayer,
        backgroundColor: Color = Color.Companion.White,
        repressMode: RepressMode = RepressMode.STOP,
        isSelectEnabled: Boolean = false,
        isSelected: Boolean = false,
        nameFontSize: TextUnit = SOUND_NAME_FONT_SIZE.sp,
        durationFontSize: TextUnit = SOUND_DURATION_FONT_SIZE.sp,
    ) : this(
        id = sound.id,
        uri = sound.uri,
        volume = sound.volume,
        duration = sound.duration,
        name = sound.name,
        backgroundColor = backgroundColor,
        repressMode = repressMode,
        isSelectEnabled = isSelectEnabled,
        isSelected = isSelected,
        nameFontSize = nameFontSize,
        durationFontSize = durationFontSize,
        playCount = sound.playCount,
        player = player,
    )
}
