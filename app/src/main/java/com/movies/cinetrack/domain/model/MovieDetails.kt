package com.movies.cinetrack.domain.model

data class MovieDetails(
    val movie: Movie,
    val runtimeMinutes: Int?,
    val genres: List<String>,
)