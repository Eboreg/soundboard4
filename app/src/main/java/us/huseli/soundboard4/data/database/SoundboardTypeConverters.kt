package us.huseli.soundboard4.data.database

import androidx.room.TypeConverter
import us.huseli.retaintheme.extensions.toInstant
import us.huseli.soundboard4.SoundSortingKey
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object SoundboardTypeConverters {
    @TypeConverter
    @JvmStatic
    fun fromDuration(value: Duration?): Long? = value?.inWholeMilliseconds

    @TypeConverter
    @JvmStatic
    fun fromInstant(value: Instant?): Long? = value?.epochSecond

    @TypeConverter
    @JvmStatic
    fun fromSoundSortingKey(value: SoundSortingKey?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun toDuration(value: Long?): Duration? = value?.milliseconds

    @TypeConverter
    @JvmStatic
    fun toInstant(value: Long?): Instant? = value?.toInstant()

    @TypeConverter
    @JvmStatic
    fun toSoundSortingKey(value: String?): SoundSortingKey? = value?.let { SoundSortingKey.valueOf(it) }
}
