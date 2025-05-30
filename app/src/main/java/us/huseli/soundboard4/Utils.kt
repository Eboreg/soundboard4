package us.huseli.soundboard4

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import us.huseli.soundboard4.Constants.SOUND_DIRNAME
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration

fun Context.getInternalSoundDirectory(): File = getDir(SOUND_DIRNAME, Context.MODE_PRIVATE)

fun Context.getSoundCacheDirectory(): File = File(cacheDir, SOUND_DIRNAME).apply { mkdirs() }

fun InputStream.copyTo(out: OutputStream): Long {
    var transferred = 0L
    val buffer = ByteArray(8192)
    var length: Int

    while (read(buffer, 0, 8192).also { length = it } >= 0) {
        out.write(buffer, 0, length)
        transferred += length
    }
    return transferred
}

fun InputStream.md5(): String {
    val buffer = ByteArray(8192)
    var length: Int
    val digest = MessageDigest.getInstance("MD5")

    while (read(buffer).also { length = it } > 0) {
        digest.update(buffer, 0, length)
    }
    return String.format("%32s", BigInteger(1, digest.digest()).toString(16).replace(" ", "0"))
}

fun Color.getContentColorFor() = if (luminance() >= 0.3) Color.Black else Color.White

fun Color.darkenOrBrighten(diff: Float = 0.25f, maxDiff: Float = 0.5f): Color {
    val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(toArgb(), it) }
    val newHsv = if (luminance() >= 0.4) floatArrayOf(
        hsv[0],
        (hsv[1] + (diff + (diff - hsv[2]).coerceAtLeast(0f)).coerceAtLeast(maxDiff)).coerceAtMost(1f),
        (hsv[2] - (diff + (hsv[1] - 1f + diff).coerceAtLeast(0f)).coerceAtMost(maxDiff)).coerceAtLeast(0f),
    ) else floatArrayOf(
        hsv[0],
        (hsv[1] - (diff + (hsv[2] - 1f + diff).coerceAtLeast(0f)).coerceAtMost(maxDiff)).coerceAtLeast(0f),
        (hsv[2] + (diff + (diff - hsv[1]).coerceAtLeast(0f)).coerceAtLeast(maxDiff)).coerceAtMost(1f),
    )

    return Color(android.graphics.Color.HSVToColor(newHsv))
}

fun randomColor() =
    android.graphics.Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

@Composable
fun rememberContentColorFor(color: Color): Color = remember(color) { color.getContentColorFor() }

private val decimalFormatInternal = DecimalFormat(".#").apply {
    decimalFormatSymbols.decimalSeparator = '.'
}

fun Duration.toShortString(): String = when {
    inWholeMilliseconds < 0 -> "0s"
    inWholeMilliseconds < 950 -> decimalFormatInternal.format(inWholeMilliseconds.toDouble() / 1000) + "s"
    else -> (inWholeMilliseconds.toDouble() / 1000).roundToInt().toString() + "s"
}

fun String.isWavMimeType() = listOf("audio/wav", "audio/wave", "audio/x-wav", "audio/vnd.wave").contains(this)
