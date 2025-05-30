package us.huseli.soundboard4.data.model

import androidx.compose.runtime.Immutable
import java.io.File
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Immutable
data class TempSound(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val uri: String = "",
    val file: File = File(""),
    val duration: Duration = 0.seconds,
    val checksum: String = "",
    val isDuplicate: Boolean = false,
    val mimeType: String = "",
)
