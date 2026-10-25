package com.idlix.tv.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Serializable
data class SeriesListResponse(
    val data: List<SeriesItem> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class SeriesItem(
    val id: String = "",
    val title: String = "",
    val slug: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val firstAirDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val numberOfSeasons: Int? = null,
    val country: String? = null,
    val contentType: String = "tv_series",
    val genres: List<Genre> = emptyList(),
    val hasVideo: Boolean = true
) {
    val fullPosterUrl: String
        get() = if (posterPath != null) "https://image.tmdb.org/t/p/w342$posterPath" else ""

    val fullBackdropUrl: String
        get() = if (backdropPath != null) "https://image.tmdb.org/t/p/w1280$backdropPath" else ""

    val formattedRating: String
        get() = voteAverage?.toDoubleOrNull()?.let { String.format("%.1f", it) } ?: "N/A"

    fun toMovieItem(): MovieItem = MovieItem(
        id = id,
        title = title,
        slug = slug,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        viewCount = viewCount,
        contentType = "tv_series"
    )
}

@Serializable
data class SeriesDetail(
    val id: String = "",
    val tmdbId: Long? = null,
    val imdbId: String? = null,
    val title: String = "",
    val slug: String = "",
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val logoPath: String? = null,
    val backdrops: List<String> = emptyList(),
    val firstAirDate: String? = null,
    val numberOfSeasons: Int = 1,
    val numberOfEpisodes: Int = 1,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val country: String? = null,
    val status: String? = null,
    val genres: List<Genre> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val defaultSeason: SeasonDetail? = null
) {
    val fullPosterUrl: String
        get() = if (posterPath != null) "https://image.tmdb.org/t/p/w342$posterPath" else ""

    val fullBackdropUrl: String
        get() = if (backdropPath != null) "https://image.tmdb.org/t/p/w1280$backdropPath" else ""
}

@Serializable
data class Season(
    val id: String = "",
    val seasonNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val posterPath: String? = null,
    val airDate: String? = null,
    val episodeCount: Int = 0
) {
    val fullPosterUrl: String
        get() = if (posterPath != null) "https://image.tmdb.org/t/p/w342$posterPath" else ""
}

@Serializable
data class SeasonDetail(
    val id: String = "",
    val seasonNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val posterPath: String? = null,
    val episodes: List<EpisodeItem> = emptyList()
)

@Serializable
data class EpisodeItem(
    val id: String = "",
    val seasonId: String = "",
    val tvSeriesId: String = "",
    val episodeNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val stillPath: String? = null,
    val airDate: String? = null,
    val runtime: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val hasVideo: Boolean = true
) {
    val fullStillUrl: String
        get() = if (stillPath != null) "https://image.tmdb.org/t/p/w500$stillPath" else ""
}

@Serializable
data class EpisodeResponse(
    val series: SeriesDetail? = null,
    val season: SeasonDetail? = null,
    val episode: EpisodeItem? = null,
    val allSeasons: List<Season> = emptyList()
)

@Serializable
data class SearchResponse(
    val results: List<SearchResultItem> = emptyList(),
    val total: Long = 0,
    @Serializable(with = FlexibleStringSerializer::class)
    val order: String? = null
)

@Serializable
data class SearchResultItem(
    val id: String = "",
    val contentType: String = "movie",
    val title: String = "",
    val originalTitle: String? = null,
    val overview: String? = null,
    val genres: List<String> = emptyList(),
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val originalLanguage: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val quality: String? = null,
    val slug: String = "",
    val numberOfSeasons: Int? = null,
    val commentCount: Int? = null,
    val country: JsonElement? = null,
    val network: JsonElement? = null,
    val networks: JsonElement? = null,
    val collection: JsonElement? = null,
    val collections: JsonElement? = null,
    val runtime: JsonElement? = null
) {
    val fullPosterUrl: String
        get() = if (posterPath != null) "https://image.tmdb.org/t/p/w342$posterPath" else ""

    val fullBackdropUrl: String
        get() = if (backdropPath != null) "https://image.tmdb.org/t/p/w1280$backdropPath" else ""

    val formattedRating: String
        get() = voteAverage?.toDoubleOrNull()?.let { String.format("%.1f", it) } ?: (voteAverage ?: "N/A")

    val countryLabel: String? get() = country.toDisplayLabel()
    val networkLabel: String? get() = network.toDisplayLabel() ?: networks.toDisplayLabel()
    val collectionLabel: String? get() = collection.toDisplayLabel() ?: collections.toDisplayLabel()
    val releaseYear: String? get() = (releaseDate ?: firstAirDate)?.take(4)?.takeIf { it.length == 4 }
    val runtimeMinutes: Int? get() = runtime.toRuntimeMinutes()
    val isAnime: Boolean
        get() = originalLanguage.equals("ja", ignoreCase = true) ||
            genres.any { it.contains("animation", ignoreCase = true) || it.contains("anime", ignoreCase = true) }
    val isKoreanDrama: Boolean
        get() = originalLanguage.equals("ko", ignoreCase = true) ||
            countryLabel.orEmpty().contains("korea", ignoreCase = true)
    val isShort: Boolean
        get() = (runtimeMinutes != null && runtimeMinutes <= 50) ||
            genres.any { it.contains("short", ignoreCase = true) }
}

private fun JsonElement?.toDisplayLabel(): String? = when (this) {
    is JsonPrimitive -> contentOrNull?.takeIf { it.isNotBlank() && it != "null" }
    is JsonObject -> listOf("name", "title", "label", "original_name")
        .firstNotNullOfOrNull { (this[it] as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank) }
    is JsonArray -> mapNotNull { it.toDisplayLabel() }.distinct().joinToString(", ").takeIf(String::isNotBlank)
    else -> null
}

private fun JsonElement?.toRuntimeMinutes(): Int? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.intOrNull ?: primitive.contentOrNull
        ?.substringBefore(" ")
        ?.toIntOrNull()
}
