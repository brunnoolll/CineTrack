package com.movies.cinetrack.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movies.cinetrack.R
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.ui.theme.CineTrackTheme
import java.time.LocalDate
import java.util.Locale

@Composable
fun MovieCard(
    movie: Movie,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val year = movie.releaseDate?.let { runCatching { LocalDate.parse(it).year.toString() }.getOrNull() }
    val poster = painterResource(R.drawable.ic_movie_placeholder)
    Card(
        modifier = modifier.fillMaxWidth().then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                contentDescription = null,
                placeholder = poster,
                error = poster,
                fallback = poster,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(112.dp).height(168.dp),
            )
            Column(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = year ?: stringResource(R.string.release_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = stringResource(R.string.rating),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = if (movie.voteAverage > 0) {
                            String.format(Locale.forLanguageTag("pt-BR"), "%.1f/10", movie.voteAverage)
                        } else stringResource(R.string.no_rating),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(name = "MovieCard", showBackground = true, backgroundColor = 0xFF0E0F13, widthDp = 360)
@Composable
private fun MovieCardPreview() {
    CineTrackTheme {
        MovieCard(
            movie = Movie(
                id = 1,
                title = "O Senhor dos Anéis: A Sociedade do Anel",
                overview = "Uma jornada pela Terra-média.",
                posterPath = null,
                backdropPath = null,
                voteAverage = 8.4,
                releaseDate = "2001-12-19",
            ),
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}