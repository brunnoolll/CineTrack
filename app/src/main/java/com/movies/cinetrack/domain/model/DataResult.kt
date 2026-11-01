package com.movies.cinetrack.domain.model

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failed(val reason: DataError) : DataResult<Nothing>
}