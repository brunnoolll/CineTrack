package com.movies.cinetrack.presentation.movies.details.contract

import com.movies.cinetrack.presentation.model.UiError

sealed interface MovieDetailsUiEffect {
    data class ShowMessage(val error: UiError) : MovieDetailsUiEffect
}