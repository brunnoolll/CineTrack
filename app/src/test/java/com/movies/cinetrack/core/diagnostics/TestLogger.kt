package com.movies.cinetrack.core.diagnostics

internal class TestLogger(
    private val onError: (String, Throwable?) -> Unit = { _, _ -> },
) : Logger {
    override fun debug(message: String, cause: Throwable?) = Unit
    override fun info(message: String, cause: Throwable?) = Unit
    override fun error(message: String, cause: Throwable?) = onError(message, cause)
}
