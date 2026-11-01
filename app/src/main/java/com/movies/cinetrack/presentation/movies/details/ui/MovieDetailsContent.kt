package com.movies.cinetrack.presentation.movies.details.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movies.cinetrack.R
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.ui.theme.CineTrackTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovieDetailsContent(
    details: MovieDetails,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val movie = details.movie
    val poster = painterResource(R.drawable.ic_movie_placeholder)
    val coverUrl = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
        ?: movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val release = movie.releaseDate?.let {
        runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
    }

    Column(
        modifier = modifier.fillMaxSize().testTag("details_content").verticalScroll(scrollState).padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            placeholder = poster, error = poster, fallback = poster,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp),
        )
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                contentDescription = null,
                placeholder = poster, error = poster, fallback = poster,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(88.dp).height(132.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(movie.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(R.drawable.ic_star), stringResource(R.string.rating), tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        if (movie.voteAverage > 0) String.format(Locale.forLanguageTag("pt-BR"), "%.1f/10", movie.voteAverage)
                        else stringResource(R.string.no_rating),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                details.runtimeMinutes?.let { Text(stringResource(R.string.runtime_minutes, it)) }
            }
        }
        Text(
            text = release?.let { stringResource(R.string.release_date, it) }
                ?: stringResource(R.string.release_unknown),
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = details.genres.takeIf { it.isNotEmpty() }?.joinToString(" • ")
                ?: stringResource(R.string.genres_unknown),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = stringResource(R.string.synopsis),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = movie.overview.takeIf { it.isNotBlank() } ?: stringResource(R.string.synopsis_unavailable),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(name = "Detalhes do filme", showBackground = true, widthDp = 360, heightDp = 800, locale = "pt-rBR")
@Preview(name = "Detalhes — texto ampliado", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f, locale = "pt-rBR")
@Composable
private fun MovieDetailsContentPreview() {
    CineTrackTheme {
        Surface {
            MovieDetailsContent(
                details = MovieDetails(
                    movie = Movie(
                        id = 1,
                        title = "O Senhor dos Anéis: A Sociedade do Anel",
                        overview = "Frodo recebe um anel poderoso e parte em uma jornada pela Terra-média. " +
                            "Ao lado de seus companheiros, precisa enfrentar perigos para impedir " +
                            "que o anel caia nas mãos do Senhor das Trevas.",
                        posterPath = null,
                        backdropPath = null,
                        voteAverage = 8.4,
                        releaseDate = "2001-12-19",
                    ),
                    runtimeMinutes = 178,
                    genres = listOf("Aventura", "Fantasia", "Ação"),
                ),
            )
        }
    }
}