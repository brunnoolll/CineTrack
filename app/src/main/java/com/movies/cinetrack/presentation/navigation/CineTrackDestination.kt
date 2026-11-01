package com.movies.cinetrack.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface CineTrackDestination {
    @Serializable data object Home : CineTrackDestination
    @Serializable data class MovieDetails(val movieId: Int) : CineTrackDestination
}