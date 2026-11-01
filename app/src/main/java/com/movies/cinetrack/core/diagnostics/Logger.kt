package com.movies.cinetrack.core.diagnostics

interface Logger {
    fun debug(message: String, cause: Throwable? = null)
    fun info(message: String, cause: Throwable? = null)
    fun error(message: String, cause: Throwable? = null)
}