package com.idlix.tv.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import com.idlix.tv.R
import com.idlix.tv.data.local.TvPreferencesStore
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.components.TvMovieCard
import com.idlix.tv.ui.components.glassSurface
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixWhite

private data class SearchFilters(
    val type: String = "all",
    val category: String = "all",
    val genre: String = "all",
    val country: String = "all",
    val year: String = "all",
    val network: String = "all",
    val sort: String = "relevance"
)

private data class FilterChoice(val label: String, val value: String)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    initialFilterPanelVisible: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by TvPreferencesStore.preferences.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isInputFocused by remember { mutableStateOf(false) }
    var isFilterPanelVisible by remember(initialFilterPanelVisible) { mutableStateOf(initialFilterPanelVisible) }
    var filters by remember { mutableStateOf(SearchFilters()) }
    val searchFocusRequester = remember { FocusRequester() }
    val filterButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            if (initialFilterPanelVisible) filterButtonFocusRequester.requestFocus()
            else searchFocusRequester.requestFocus()
        } catch (_: Exception) {
            // Focus can briefly be unavailable while the navigation transition settles.
        }
    }

    val quickSearches = listOf("Batman", "Spider-Man", "Action", "Korea", "Horror", "Comedy", "Anime", "Romance")
    val genres = uiState.results.flatMap { it.genres }.map(String::trim).filter(String::isNotBlank).distinct().sorted()
    val countries = uiState.results.mapNotNull { it.countryLabel?.trim()?.takeIf(String::isNotBlank) }.distinct().sorted()
    val years = uiState.results.mapNotNull { it.releaseYear }.distinct().sortedDescending()
    val networks = uiState.results.mapNotNull { it.networkLabel?.trim()?.takeIf(String::isNotBlank) }.distinct().sorted()

    val filteredResults = remember(uiState.results, filters) {
        val filtered = uiState.results.filter { item ->
            val typeMatch = when (filters.type) {
                "movie" -> item.contentType.equals("movie", ignoreCase = true)
                "series" -> item.contentType.equals("tv_series", ignoreCase = true) || item.contentType.equals("series", ignoreCase = true)
                else -> true
            }
            val categoryMatch = when (filters.category) {
                "anime" -> item.isAnime
                "kdrama" -> item.isKoreanDrama
                "shorts" -> item.isShort
                "collections" -> !item.collectionLabel.isNullOrBlank()
                else -> true
            }
            val genreMatch = filters.genre == "all" || item.genres.any { it.normalizedFilterValue() == filters.genre.normalizedFilterValue() }
            val countryMatch = filters.country == "all" || item.countryLabel?.contains(filters.country, ignoreCase = true) == true
            val yearMatch = filters.year == "all" || item.releaseYear == filters.year
            val networkMatch = filters.network == "all" || item.networkLabel?.contains(filters.network, ignoreCase = true) == true
            typeMatch && categoryMatch && genreMatch && countryMatch && yearMatch && networkMatch
        }
        when (filters.sort) {
            "popular" -> filtered.sortedByDescending { it.viewCount }
            "rating" -> filtered.sortedByDescending { it.voteAverage?.toDoubleOrNull() ?: 0.0 }
            "newest" -> filtered.sortedByDescending { it.releaseDate ?: it.firstAirDate ?: "" }
            else -> filtered
        }
    }

    val typeChoices = listOf(
        FilterChoice(stringResource(R.string.filter_all), "all"),
        FilterChoice(stringResource(R.string.filter_movies), "movie"),
        FilterChoice(stringResource(R.string.filter_series), "series")
    )
    val categoryChoices = listOf(
        FilterChoice(stringResource(R.string.filter_all_categories), "all"),
        FilterChoice(stringResource(R.string.filter_anime), "anime"),
        FilterChoice(stringResource(R.string.filter_korean_drama), "kdrama"),
        FilterChoice(stringResource(R.string.filter_shorts), "shorts"),
        FilterChoice(stringResource(R.string.filter_collections), "collections")
    )
    val genreChoices = listOf(FilterChoice(stringResource(R.string.filter_all), "all")) +
        (genres.ifEmpty { listOf("Action", "Adventure", "Animation", "Comedy", "Crime", "Drama", "Fantasy", "Horror", "Romance", "Science Fiction", "Thriller") })
            .map { FilterChoice(it, it) }
    val countryChoices = listOf(FilterChoice(stringResource(R.string.filter_all), "all")) +
        (countries.ifEmpty { listOf("Indonesia", "Japan", "South Korea", "United States", "United Kingdom", "China", "Thailand") })
            .map { FilterChoice(it, it) }
    val yearChoices = listOf(FilterChoice(stringResource(R.string.filter_all), "all")) + years.map { FilterChoice(it, it) }
    val networkChoices = listOf(FilterChoice(stringResource(R.string.filter_all), "all")) + networks.map { FilterChoice(it, it) }
    val sortChoices = listOf(
        FilterChoice(stringResource(R.string.filter_relevance), "relevance"),
        FilterChoice(stringResource(R.string.filter_popular), "popular"),
        FilterChoice(stringResource(R.string.filter_rating), "rating"),
        FilterChoice(stringResource(R.string.filter_newest), "newest")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        if (initialFilterPanelVisible) {
            Text(
                text = stringResource(R.string.explore_title),
                color = NetflixWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .glassSurface(preferences.glassIntensity, RoundedCornerShape(18.dp))
                .border(
                    width = if (isInputFocused) 2.dp else 0.dp,
                    color = if (isInputFocused) androidx.compose.ui.graphics.Color(0xFFB7D8FF) else androidx.compose.ui.graphics.Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isInputFocused) NetflixWhite else NetflixLightGray
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                textStyle = TextStyle(color = NetflixWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                cursorBrush = SolidColor(androidx.compose.ui.graphics.Color(0xFF9ECFFF)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .then(if (initialFilterPanelVisible) Modifier else Modifier.focusRequester(searchFocusRequester))
                    .onFocusChanged { isInputFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (uiState.query.isEmpty()) {
                        Text(stringResource(R.string.search_placeholder), color = NetflixLightGray, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            if (uiState.query.isNotEmpty()) {
                Button(
                    onClick = { viewModel.onQueryChange("") },
                    colors = ButtonDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = NetflixLightGray,
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF78A8D7),
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(R.string.search_clear_filters))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.popular_searches),
                color = NetflixLightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickSearches) { tag ->
                    Button(
                        onClick = { viewModel.onQueryChange(tag) },
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixDarkCard.copy(alpha = 0.75f),
                            contentColor = NetflixLightGray,
                            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF78A8D7),
                            focusedContentColor = NetflixBlack
                        ),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(14.dp))
                    ) {
                        Text(text = tag, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { isFilterPanelVisible = !isFilterPanelVisible },
                colors = ButtonDefaults.colors(
                    containerColor = if (isFilterPanelVisible) androidx.compose.ui.graphics.Color(0xFF456E9D) else NetflixDarkCard.copy(alpha = 0.8f),
                    contentColor = NetflixWhite,
                    focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF78A8D7),
                    focusedContentColor = NetflixBlack
                ),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(14.dp)),
                modifier = if (initialFilterPanelVisible) Modifier.focusRequester(filterButtonFocusRequester) else Modifier
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FilterAlt, contentDescription = null)
                    Text(stringResource(R.string.search_filters), fontSize = 12.sp)
                }
            }
            if (filters != SearchFilters()) {
                Button(
                    onClick = { filters = SearchFilters() },
                    colors = ButtonDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = NetflixLightGray,
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF78A8D7),
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(14.dp))
                ) {
                    Text(stringResource(R.string.search_clear_filters), fontSize = 12.sp)
                }
            }
            if (uiState.query.isNotBlank() && !uiState.isLoading) {
                Text(
                    text = stringResource(R.string.search_results_count, filteredResults.size),
                    color = NetflixLightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        if (isFilterPanelVisible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
                    .glassSurface(preferences.glassIntensity, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChoiceRow(stringResource(R.string.filter_type), typeChoices, filters.type) { filters = filters.copy(type = it) }
                FilterChoiceRow(stringResource(R.string.filter_category), categoryChoices, filters.category) { filters = filters.copy(category = it) }
                FilterChoiceRow(stringResource(R.string.filter_genre), genreChoices, filters.genre) { filters = filters.copy(genre = it) }
                FilterChoiceRow(stringResource(R.string.filter_country), countryChoices, filters.country) { filters = filters.copy(country = it) }
                if (yearChoices.size > 1) {
                    FilterChoiceRow(stringResource(R.string.filter_year), yearChoices, filters.year) { filters = filters.copy(year = it) }
                }
                if (networkChoices.size > 1) {
                    FilterChoiceRow(stringResource(R.string.filter_network), networkChoices, filters.network) { filters = filters.copy(network = it) }
                }
                FilterChoiceRow(stringResource(R.string.filter_sort), sortChoices, filters.sort) { filters = filters.copy(sort = it) }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> TvLoadingSpinner(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = uiState.error.orEmpty(),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.results.isEmpty() && uiState.query.isBlank() -> Text(
                    text = stringResource(R.string.search_hint),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                filteredResults.isEmpty() && uiState.results.isNotEmpty() && filters != SearchFilters() -> Text(
                    text = stringResource(R.string.search_filter_empty),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                filteredResults.isEmpty() -> Text(
                    text = stringResource(R.string.no_results_for, uiState.query),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> TvLazyVerticalGrid(
                    columns = TvGridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults, key = { "${it.contentType}-${it.id}-${it.slug}" }) { item ->
                        TvMovieCard(
                            title = item.title,
                            posterUrl = item.fullPosterUrl,
                            rating = item.formattedRating,
                            quality = item.quality ?: if (item.contentType == "tv_series") "SERIES" else "MOVIE",
                            onClick = { onNavigateToDetail(item.contentType, item.slug) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FilterChoiceRow(
    title: String,
    choices: List<FilterChoice>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = NetflixLightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(96.dp)
        )
        TvLazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(choices, key = { it.value }) { choice ->
                val isSelected = choice.value == selected
                Button(
                    onClick = { onSelected(choice.value) },
                    colors = ButtonDefaults.colors(
                        containerColor = if (isSelected) androidx.compose.ui.graphics.Color(0xFF456E9D).copy(alpha = 0.8f) else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.055f),
                        contentColor = if (isSelected) NetflixWhite else NetflixLightGray,
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF9ECFFF),
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(choice.label, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

private fun String.normalizedFilterValue(): String =
    lowercase().replace('-', ' ').replace('_', ' ').trim()
