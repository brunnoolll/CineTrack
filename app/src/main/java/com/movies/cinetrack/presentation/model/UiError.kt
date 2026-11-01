package com.movies.cinetrack.presentation.model

import androidx.annotation.StringRes
import com.movies.cinetrack.R
import com.movies.cinetrack.domain.model.DataError

enum class UiError {
    NETWORK,
    UNAUTHORIZED,
    RATE_LIMITED,
    NOT_FOUND,
    SERVER,
    UNKNOWN,
}

internal fun DataError.toUiError(): UiError = when (this) {
    DataError.NETWORK -> UiError.NETWORK
    DataError.UNAUTHORIZED -> UiError.UNAUTHORIZED
    DataError.RATE_LIMITED -> UiError.RATE_LIMITED
    DataError.NOT_FOUND -> UiError.NOT_FOUND
    DataError.SERVER -> UiError.SERVER
    DataError.UNKNOWN -> UiError.UNKNOWN
}

@StringRes
internal fun UiError.messageResId(): Int = when (this) {
    UiError.NETWORK -> R.string.error_network
    UiError.UNAUTHORIZED -> R.string.error_unknown
    UiError.RATE_LIMITED -> R.string.error_rate_limited
    UiError.NOT_FOUND -> R.string.error_not_found
    UiError.SERVER -> R.string.error_unknown
    UiError.UNKNOWN -> R.string.error_unknown
}