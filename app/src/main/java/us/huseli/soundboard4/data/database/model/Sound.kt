package us.huseli.soundboard4.data.database.model

import androidx.compose.runtime.Immutable
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import us.huseli.soundboard4.SoundSortingKey
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Entity(
    tableName = "sounds",
    indices = [Index("categoryId")],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
@Immutable
data class Sound(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val uri: String = "",
    val duration: Duration = 0.seconds,
    val volume: Float = 1f,
    val categoryId: String = UUID.randomUUID().toString(),
    val checksum: String = "",
    val created: Instant = Instant.now(),
    val playCount: Int = 0,
    val mimeType: String = "",
) {
    val file: File
        get() = uri.toUri().buildUpon().scheme("file").build().toFile()
}

class SoundComparator(private val key: SoundSortingKey, private val ascending: Boolean) : Comparator<Sound> {
    override fun compare(o1: Sound, o2: Sound): Int {
        val sound1 = if (ascending) o1 else o2
        val sound2 = if (ascending) o2 else o1

        return when (key) {
            SoundSortingKey.NAME -> sound1.name.compareTo(sound2.name, true)
            SoundSortingKey.LENGTH -> sound1.duration.compareTo(sound2.duration)
            SoundSortingKey.CREATION_TIME -> sound1.created.compareTo(sound2.created)
            SoundSortingKey.PLAY_COUNT -> sound1.playCount.compareTo(sound2.playCount)
        }
    }
}

fun Iterable<Sound>.filterBySearchTerm(searchTerm: String? = null): List<Sound> =
    filter { searchTerm == null || it.name.contains(searchTerm, true) }
