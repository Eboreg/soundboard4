package us.huseli.soundboard4.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import us.huseli.soundboard4.Constants.DEFAULT_COLUMN_COUNT_PORTRAIT
import us.huseli.soundboard4.Constants.PREF_AUTO_IMPORT
import us.huseli.soundboard4.Constants.PREF_AUTO_IMPORT_CATEGORY_ID
import us.huseli.soundboard4.Constants.PREF_AUTO_IMPORT_DIRECTORY
import us.huseli.soundboard4.Constants.PREF_COLUMN_COUNT_PORTRAIT
import us.huseli.soundboard4.Constants.PREF_CONVERT_TO_WAV
import us.huseli.soundboard4.Constants.PREF_REPRESS_MODE
import us.huseli.soundboard4.RepressMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {
    data class ColumnInfo(val count: Int, val widthDp: Int)

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val _repressMode = MutableStateFlow(
        preferences.getString(PREF_REPRESS_MODE, null)?.let { RepressMode.valueOf(it) } ?: RepressMode.STOP
    )
    private val _columnCountLandscape = MutableStateFlow(
        portraitColumnCountToLandscape(preferences.getInt(PREF_COLUMN_COUNT_PORTRAIT, DEFAULT_COLUMN_COUNT_PORTRAIT))
    )
    private val _columnCountPortrait =
        MutableStateFlow(preferences.getInt(PREF_COLUMN_COUNT_PORTRAIT, DEFAULT_COLUMN_COUNT_PORTRAIT))
    private val _orientation = MutableStateFlow(context.resources.configuration.orientation)
    private val _screenWidthDp = MutableStateFlow(context.resources.configuration.screenWidthDp)
    private val _autoImport = MutableStateFlow(preferences.getBoolean(PREF_AUTO_IMPORT, false))
    private val _autoImportDirectory = MutableStateFlow(
        preferences.getString(PREF_AUTO_IMPORT_DIRECTORY, null)?.toUri()
    )
    private val _autoImportCategoryId = MutableStateFlow(preferences.getString(PREF_AUTO_IMPORT_CATEGORY_ID, null))
    private val _convertToWav = MutableStateFlow(preferences.getBoolean(PREF_CONVERT_TO_WAV, true))

    val autoImport = _autoImport.asStateFlow()
    val autoImportDirectory = _autoImportDirectory.asStateFlow()
    val autoImportCategoryId = _autoImportCategoryId.asStateFlow()
    val convertToWav = _convertToWav.asStateFlow()
    val repressMode = _repressMode.asStateFlow()
    val columnInfo = combine(
        _columnCountPortrait,
        _columnCountLandscape,
        _orientation,
        _screenWidthDp,
    ) { columnCountPortrait, columnCountLandscape, orientation, screenWidthDp ->
        val columnCount = when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> columnCountPortrait
            else -> columnCountLandscape
        }
        val gaps = 5 * (columnCount - 1)
        val columnWidth = (screenWidthDp - gaps) / columnCount

        ColumnInfo(count = columnCount, widthDp = columnWidth)
    }
    val canZoomIn = columnInfo.map { it.count > 1 }

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun setAutoImport(value: Boolean) = preferences.edit { putBoolean(PREF_AUTO_IMPORT, value) }

    fun setAutoImportCategoryId(value: String) = preferences.edit { putString(PREF_AUTO_IMPORT_CATEGORY_ID, value) }

    fun setAutoImportDirectory(value: Uri) {
        context.contentResolver.takePersistableUriPermission(value, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        preferences.edit { putString(PREF_AUTO_IMPORT_DIRECTORY, value.toString()) }
    }

    fun setConvertToWav(value: Boolean) = preferences.edit { putBoolean(PREF_CONVERT_TO_WAV, value) }

    fun setOrientation(value: Int) {
        _orientation.value = value
    }

    fun setRepressMode(value: RepressMode) = preferences.edit { putString(PREF_REPRESS_MODE, value.name) }

    fun setScreenWidthDp(value: Int) {
        _screenWidthDp.value = value
    }

    fun zoomIn(): Int = zoom(-1)

    fun zoomOut(): Int = zoom(1)

    private fun getScreenRatio(): Double {
        val width = context.resources.configuration.screenWidthDp.toDouble()
        val height = context.resources.configuration.screenHeightDp.toDouble()

        return min(height, width) / max(height, width)
    }

    private fun getZoomPercent(): Int = when (_orientation.value) {
        Configuration.ORIENTATION_PORTRAIT ->
            (DEFAULT_COLUMN_COUNT_PORTRAIT.toDouble() / _columnCountPortrait.value * 100).roundToInt()
        else ->
            (portraitColumnCountToLandscape(DEFAULT_COLUMN_COUNT_PORTRAIT).toDouble() / _columnCountLandscape.value * 100).roundToInt()
    }

    private fun landscapeColumnCountToPortrait(columnCount: Int) =
        (columnCount * getScreenRatio()).roundToInt().coerceAtLeast(1)

    private fun portraitColumnCountToLandscape(columnCount: Int) =
        (columnCount / getScreenRatio()).roundToInt().coerceAtLeast(1)

    private fun zoom(factor: Int): Int {
        when (_orientation.value) {
            Configuration.ORIENTATION_PORTRAIT -> {
                _columnCountPortrait.value = (_columnCountPortrait.value + factor).coerceAtLeast(1)
                _columnCountLandscape.value = portraitColumnCountToLandscape(_columnCountPortrait.value)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                _columnCountLandscape.value = (_columnCountLandscape.value + factor).coerceAtLeast(1)
                _columnCountPortrait.value = landscapeColumnCountToPortrait(_columnCountLandscape.value)
            }
        }
        preferences.edit { putInt(PREF_COLUMN_COUNT_PORTRAIT, _columnCountPortrait.value) }
        return getZoomPercent()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_REPRESS_MODE ->
                preferences.getString(key, null)?.let { RepressMode.valueOf(it) }?.also { _repressMode.value = it }
            PREF_COLUMN_COUNT_PORTRAIT -> preferences.getInt(key, 0).takeIf { it > 0 }?.also {
                _columnCountPortrait.value = it
            }
            PREF_AUTO_IMPORT -> _autoImport.value = preferences.getBoolean(key, false)
            PREF_AUTO_IMPORT_DIRECTORY -> _autoImportDirectory.value = preferences.getString(key, null)?.toUri()
            PREF_AUTO_IMPORT_CATEGORY_ID -> _autoImportCategoryId.value = preferences.getString(key, null)
            PREF_CONVERT_TO_WAV -> _convertToWav.value = preferences.getBoolean(key, true)
        }
    }
}
