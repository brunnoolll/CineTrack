package com.movies.cinetrack.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    /** Data ISO yyyy-MM-dd, quando disponível. */
    val releaseDate: String?,
)