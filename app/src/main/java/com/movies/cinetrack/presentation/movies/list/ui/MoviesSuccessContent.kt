package com.movies.cinetrack.presentation.movies.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.movies.cinetrack.R
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.presentation.components.MovieCard
import com.movies.cinetrack.ui.theme.CineTrackTheme

@Composable
fun MoviesSuccessContent(
    movies: List<Movie>,
    onMovieSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    bottomContentPadding: Dp = 0.dp,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("movies_list"),
        state = listState,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 20.dp + bottomContentPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (movies.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_movies), style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            items(movies, key = { it.id }) { movie ->
                MovieCard(movie = movie, onClick = { onMovieSelected(movie.id) })
            }
        }
    }
}

@Preview(name = "Filmes", showBackground = true, widthDp = 360, heightDp = 800, locale = "pt-rBR")
@Composable
private fun MoviesSuccessContentPreview() {
    CineTrackTheme {
        Surface {
            MoviesSuccessContent(
                movies = listOf(
                    Movie(
                        id = 1,
                        title = "O Senhor dos Anéis: A Sociedade do Anel",
                        overview = "Uma jornada pela Terra-média.",
                        posterPath = null,
                        backdropPath = null,
                        voteAverage = 8.4,
                        releaseDate = "2001-12-19",
                    ),
                    Movie(
                        id = 2,
                        title = "A Viagem de Chihiro",
                        overview = "Uma menina descobre um mundo mágico.",
                        posterPath = null,
                        backdropPath = null,
                        voteAverage = 8.5,
                        releaseDate = "2001-07-20",
                    ),
                    Movie(
                        id = 3,
                        title = "Filme sem avaliação e data de lançamento",
                        overview = "",
                        posterPath = null,
                        backdropPath = null,
                        voteAverage = 0.0,
                        releaseDate = null,
                    ),
                ),
                onMovieSelected = {},
            )
        }
    }
}

@Preview(name = "Filmes — lista vazia", showBackground = true, widthDp = 360, heightDp = 800, locale = "pt-rBR")
@Composable
private fun MoviesEmptyContentPreview() {
    CineTrackTheme {
        Surface {
            MoviesSuccessContent(movies = emptyList(), onMovieSelected = {})
        }
    }
}