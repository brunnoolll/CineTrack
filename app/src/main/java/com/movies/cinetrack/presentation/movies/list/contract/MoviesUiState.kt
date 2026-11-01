package com.movies.cinetrack.presentation.movies.list.contract

import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.presentation.model.UiError

sealed class MoviesUiState {
    data class Loading(val movies: List<Movie>? = null) : MoviesUiState()
    data class Success(val movies: List<Movie>) : MoviesUiState()
    data class Failure(val error: UiError) : MoviesUiState()
}