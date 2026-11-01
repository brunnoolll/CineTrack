package com.movies.cinetrack.presentation.movies.details.contract

sealed interface MovieDetailsUiAction {
    object Refresh : MovieDetailsUiAction
    object Retry : MovieDetailsUiAction
}