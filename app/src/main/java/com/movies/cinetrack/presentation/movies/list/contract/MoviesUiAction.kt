package com.movies.cinetrack.presentation.movies.list.contract

sealed interface MoviesUiAction {
    object Refresh : MoviesUiAction
    object Retry : MoviesUiAction
}