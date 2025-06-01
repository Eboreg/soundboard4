package us.huseli.soundboard4.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.arthenica.ffmpegkit.FFmpegKit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import us.huseli.retaintheme.snackbar.SnackbarEngine
import us.huseli.retaintheme.utils.AbstractBaseViewModel
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.repository.CategoryRepository
import us.huseli.soundboard4.data.repository.SettingsRepository
import us.huseli.soundboard4.data.repository.SoundRepository
import us.huseli.soundboard4.domain.AutoSoundImportUseCase
import us.huseli.soundboard4.getInternalSoundDirectory
import us.huseli.soundboard4.isWavMimeType
import us.huseli.soundboard4.ui.states.SettingsUiState
import us.huseli.soundboard4.ui.utils.WorkInProgressState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
    private val soundRepository: SoundRepository,
    private val autoSoundImportUseCase: AutoSoundImportUseCase,
) : AbstractBaseViewModel() {
    inner class AutoImportDirectoryContract : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            val intent = super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

            settingsRepository.autoImportDirectory.value?.also {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
            }
            return intent
        }
    }

    val uiState = combine(
        settingsRepository.autoImport,
        settingsRepository.autoImportDirectory,
        settingsRepository.autoImportCategoryId,
        settingsRepository.convertToWav,
        categoryRepository.flowAll(),
    ) { autoImport, autoImportDirectory, autoImportCategoryId, convertToWav, categories ->
        SettingsUiState(
            autoImport = autoImport,
            autoImportDirectory = autoImportDirectory,
            categories = categories,
            autoImportCategory = categories.find { it.id == autoImportCategoryId },
            convertToWav = convertToWav,
        )
    }.stateWhileSubscribed(SettingsUiState())

    suspend fun convertExistingFilesToWav(context: Context, wipState: WorkInProgressState? = null) {
        val sounds = soundRepository.listAll().filterNot { it.mimeType.isWavMimeType() }
        var convertedFiles = 0
        val errors = mutableListOf<String>()

        for (sound in sounds) {
            val stem = sound.file.nameWithoutExtension
            val outFile = File(context.getInternalSoundDirectory(), "$stem.wav")
            val session = FFmpegKit.execute("-i \"${sound.file.path}\" -y \"${outFile.path}\"")

            wipState?.addStatusRow(context.getString(R.string.converting_x, sound.name))

            if (session.returnCode.isValueSuccess) {
                sound.file.delete()
                soundRepository.update(
                    sound.copy(
                        uri = outFile.toUri().toString(),
                        mimeType = "audio/x-wav",
                    )
                )
                convertedFiles++
            } else if (session.returnCode.isValueError) {
                errors.add(session.logsAsString)
            }
        }

        if (convertedFiles > 0) SnackbarEngine.addInfo(
            context.resources.getQuantityString(
                R.plurals.converted_x_files_to_wav,
                convertedFiles,
                convertedFiles,
            )
        )
        if (errors.isNotEmpty()) SnackbarEngine.addError(
            context.resources.getQuantityString(
                R.plurals.failed_converting_x_files_to_wav,
                errors.size,
                errors.size,
            )
        )
    }

    fun setAutoImport(value: Boolean) = settingsRepository.setAutoImport(value)

    fun setAutoImportCategoryId(value: String) = settingsRepository.setAutoImportCategoryId(value)

    fun setAutoImportDirectory(value: Uri) = settingsRepository.setAutoImportDirectory(value)

    fun setConvertToWav(value: Boolean) = settingsRepository.setConvertToWav(value)

    fun startAutoSoundImport() = autoSoundImportUseCase()
}
