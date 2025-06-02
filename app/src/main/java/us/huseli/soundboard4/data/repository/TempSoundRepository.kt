package us.huseli.soundboard4.data.repository

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.data.model.TempSound
import us.huseli.soundboard4.getAnnotatedString
import us.huseli.soundboard4.getInternalSoundDirectory
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempSoundRepository @Inject constructor(@ApplicationContext private val context: Context) {
    val tempSounds = MutableStateFlow<List<TempSound>>(emptyList())
    val errors = MutableStateFlow<List<String>>(emptyList())

    fun clear() {
        for (tempSound in tempSounds.value) {
            tempSound.file.delete()
        }
        tempSounds.value = emptyList()
        errors.value = emptyList()
    }

    fun convertToSounds(
        tempSounds: Collection<TempSound>,
        categoryId: String,
        wipState: WorkInProgressState? = null,
    ): List<Sound> {
        return tempSounds.map { tempSound ->
            val outFile = File(context.getInternalSoundDirectory(), tempSound.file.name)

            wipState?.addStatusRow(context.getAnnotatedString(R.string.saving_x, tempSound.name))
            tempSound.file.renameTo(outFile)
            Sound(
                id = tempSound.id,
                name = tempSound.name,
                uri = outFile.toUri().toString(),
                duration = tempSound.duration,
                categoryId = categoryId,
                checksum = tempSound.checksum,
                mimeType = tempSound.mimeType,
            )
        }
    }

    fun isEmpty(): Boolean = tempSounds.value.isEmpty() && errors.value.isEmpty()

    fun isNotEmpty(): Boolean = !isEmpty()
}
