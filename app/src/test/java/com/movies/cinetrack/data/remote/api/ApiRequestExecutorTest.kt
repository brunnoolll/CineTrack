package com.movies.cinetrack.data.remote.api

import com.movies.cinetrack.core.diagnostics.TestLogger
import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiRequestExecutorTest {
    private val executor = ApiRequestExecutor(TestLogger())

    @Test
    fun successIsPreservedAfterSuspension() = runTest {
        val expected = DataResult.Success("payload")
        val result = executor.execute("test_request") { delay(1); expected }
        assertEquals(expected, result)
    }

    @Test
    fun validationFailureReturnedByBlockIsPreserved() = runTest {
        val expected = DataResult.Failed(DataError.NOT_FOUND)
        assertEquals(expected, executor.execute<String>("test_request") { expected })
    }

    @Test
    fun httpStatusIsTranslatedToDomainError() = runTest {
        val cases = mapOf(
            401 to DataError.UNAUTHORIZED,
            403 to DataError.UNAUTHORIZED,
            404 to DataError.NOT_FOUND,
            429 to DataError.RATE_LIMITED,
            500 to DataError.SERVER,
            503 to DataError.SERVER,
            599 to DataError.SERVER,
            400 to DataError.UNKNOWN,
            499 to DataError.UNKNOWN,
            600 to DataError.UNKNOWN,
        )
        for ((status, expected) in cases) {
            val error = HttpException(Response.error<Any>(status, "{}".toResponseBody()))
            val result = executor.execute<String>("test_request") {
                throw error
            }
            assertEquals(DataResult.Failed(expected), result)
        }
    }

    @Test
    fun ioFailureBecomesNetworkError() = runTest {
        val error = IOException("offline")
        val result = executor.execute<String>("test_request") { throw error }
        assertEquals(DataResult.Failed(DataError.NETWORK), result)
    }

    @Test
    fun malformedResponseBecomesUnknownError() = runTest {
        val error = SerializationException("invalid response")
        val result = executor.execute<String>("test_request") { throw error }
        assertEquals(DataResult.Failed(DataError.UNKNOWN), result)
    }

    @Test
    fun cancellationIsRethrownUnchanged() = runTest {
        val cancellation = CancellationException("cancelled")
        try {
            executor.execute<String>("test_request") { throw cancellation }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    @Test
    fun unexpectedProgrammingErrorIsNotDisguisedAsApiFailure() = runTest {
        val error = IllegalStateException("unexpected")
        try {
            executor.execute<String>("test_request") { throw error }
            fail("Unexpected exceptions must propagate")
        } catch (actual: IllegalStateException) {
            assertSame(error, actual)
        }
    }
}
