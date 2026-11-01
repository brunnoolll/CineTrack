package com.movies.cinetrack.presentation.movies.details.contract

import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.presentation.model.UiError

sealed class MovieDetailsUiState {
    data class Loading(val movie: MovieDetails? = null) : MovieDetailsUiState()
    data class Success(val movie: MovieDetails) : MovieDetailsUiState()
    data class Failure(val error: UiError) : MovieDetailsUiState()
}