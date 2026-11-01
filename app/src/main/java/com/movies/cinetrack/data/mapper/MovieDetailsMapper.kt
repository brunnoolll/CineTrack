package com.movies.cinetrack.data.mapper

import com.movies.cinetrack.data.remote.dto.MovieDetailsDto
import com.movies.cinetrack.data.remote.dto.MovieDto
import com.movies.cinetrack.domain.model.MovieDetails

fun MovieDetailsDto.toDomain(): MovieDetails = MovieDetails(
    movie = MovieDto(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
    ).toDomain(),
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = genres.orEmpty().mapNotNull { it.name?.trim()?.takeIf { name -> name.isNotEmpty() } }.distinct(),
)