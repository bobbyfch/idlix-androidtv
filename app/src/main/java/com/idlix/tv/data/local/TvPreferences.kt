package com.idlix.tv.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubtitlePosition(val bottomPaddingFraction: Float) {
    LOW(0.035f),
    STANDARD(0.085f),
    HIGH(0.16f)
}

enum class SubtitleSize(val scale: Float) {
    SMALL(0.86f),
    STANDARD(1f),
    LARGE(1.18f)
}

data class TvPreferences(
    val glassIntensity: Int = 2,
    val subtitlePosition: SubtitlePosition = SubtitlePosition.STANDARD,
    val subtitleSize: SubtitleSize = SubtitleSize.STANDARD
)

/** Shared, persisted TV preferences used by Settings, navigation and the player. */
object TvPreferencesStore {
    private const val PREFS_NAME = "idlix_tv_preferences"
    private const val KEY_GLASS_INTENSITY = "glass_intensity"
    private const val KEY_SUBTITLE_POSITION = "subtitle_position"
    private const val KEY_SUBTITLE_SIZE = "subtitle_size"

    private val mutablePreferences = MutableStateFlow(TvPreferences())
    val preferences: StateFlow<TvPreferences> = mutablePreferences.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        mutablePreferences.value = TvPreferences(
            glassIntensity = prefs.getInt(KEY_GLASS_INTENSITY, TvPreferences().glassIntensity).coerceIn(1, 3),
            subtitlePosition = enumValueOrDefault(
                prefs.getString(KEY_SUBTITLE_POSITION, null),
                SubtitlePosition.STANDARD
            ),
            subtitleSize = enumValueOrDefault(
                prefs.getString(KEY_SUBTITLE_SIZE, null),
                SubtitleSize.STANDARD
            )
        )
    }

    fun setGlassIntensity(context: Context, value: Int) = update(context) {
        it.copy(glassIntensity = value.coerceIn(1, 3))
    }

    fun setSubtitlePosition(context: Context, value: SubtitlePosition) = update(context) {
        it.copy(subtitlePosition = value)
    }

    fun setSubtitleSize(context: Context, value: SubtitleSize) = update(context) {
        it.copy(subtitleSize = value)
    }

    private fun update(context: Context, transform: (TvPreferences) -> TvPreferences) {
        val updated = transform(mutablePreferences.value)
        mutablePreferences.value = updated
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_GLASS_INTENSITY, updated.glassIntensity)
            .putString(KEY_SUBTITLE_POSITION, updated.subtitlePosition.name)
            .putString(KEY_SUBTITLE_SIZE, updated.subtitleSize.name)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default
}
