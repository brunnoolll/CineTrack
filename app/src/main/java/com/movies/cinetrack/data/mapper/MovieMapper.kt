package com.movies.cinetrack.data.mapper

import com.movies.cinetrack.data.remote.dto.MovieDto
import com.movies.cinetrack.domain.model.Movie

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview.orEmpty(),
    posterPath = posterPath?.takeIf { it.isNotBlank() },
    backdropPath = backdropPath?.takeIf { it.isNotBlank() },
    voteAverage = voteAverage ?: 0.0,
    releaseDate = releaseDate?.takeIf { it.isNotBlank() },
)