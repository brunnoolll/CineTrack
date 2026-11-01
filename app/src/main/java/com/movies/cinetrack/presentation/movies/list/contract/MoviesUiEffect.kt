package com.movies.cinetrack.presentation.movies.list.contract

import com.movies.cinetrack.presentation.model.UiError

sealed interface MoviesUiEffect {
    data class ShowMessage(val error: UiError) : MoviesUiEffect
}