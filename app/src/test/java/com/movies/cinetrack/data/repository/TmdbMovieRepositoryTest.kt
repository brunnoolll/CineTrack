package com.movies.cinetrack.data.repository

import com.movies.cinetrack.core.diagnostics.TestLogger
import com.movies.cinetrack.data.remote.api.TmdbApi
import com.movies.cinetrack.data.remote.api.ApiRequestExecutor
import com.movies.cinetrack.data.remote.dto.MovieDetailsDto
import com.movies.cinetrack.data.remote.dto.MovieDto
import com.movies.cinetrack.data.remote.dto.MoviesResponse
import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TmdbMovieRepositoryTest {
    private val api = FakeApi()

    private val executor = ApiRequestExecutor(
        TestLogger()
    )

    private val repository = TmdbMovieRepository(
        api,
        executor
    )

    @Test fun discoverRequestsOnlySelectedPageWithinFirst20() = runTest {
        api.discover = { page -> MoviesResponse(page, listOf(MovieDto(page, "Selected")), 800) }
        val result = repository.getMovies() as DataResult.Success<List<Movie>>
        val requestedPage = api.pages.single()
        assertTrue(requestedPage in 1..20)
        assertEquals(requestedPage, result.data.single().id)
    }

    @Test fun everyNewCatalogRequestConsultsApi() = runTest {
        api.discover = { page -> MoviesResponse(page, listOf(MovieDto(1, "Primeira seleção")), 20) }
        val first = repository.getMovies() as DataResult.Success<List<Movie>>
        api.discover = { page -> MoviesResponse(page, listOf(MovieDto(2, "Nova seleção")), 20) }
        val second = repository.getMovies() as DataResult.Success<List<Movie>>
        assertEquals(1, first.data.single().id)
        assertEquals(2, second.data.single().id)
        assertEquals(2, api.pages.size)
        assertTrue(api.pages.all { it in 1..20 })
    }

    @Test fun selectedPageFailureReturnsNetworkError() = runTest {
        api.discover = { throw IOException("offline") }
        assertEquals(DataResult.Failed(DataError.NETWORK), repository.getMovies())
        assertEquals(1, api.pages.size)
    }

    @Test fun emptySelectedPageReturnsEmptyListWithoutFallbackRequest() = runTest {
        api.discover = { page -> MoviesResponse(page, emptyList(), 0) }
        assertEquals(DataResult.Success(emptyList<Movie>()), repository.getMovies())
        assertEquals(1, api.pages.size)
    }

    @Test fun incorrectPageInResponseIsRejected() = runTest {
        api.discover = { page -> MoviesResponse(page + 1, listOf(MovieDto(1, "Wrong page")), 800) }
        assertEquals(DataResult.Failed(DataError.UNKNOWN), repository.getMovies())
        assertEquals(1, api.pages.size)
    }

    @Test fun detailsAreMappedAndAlwaysRequestedOnline() = runTest {
        val first = repository.getMovieDetails(42) as DataResult.Success<MovieDetails>
        assertEquals(42, first.data.movie.id)
        assertEquals(117, first.data.runtimeMinutes)
        api.details = { id -> MovieDetailsDto(id, "Detalhes atualizados", runtime = 120) }
        val second = repository.getMovieDetails(42) as DataResult.Success<MovieDetails>
        assertEquals("Detalhes atualizados", second.data.movie.title)
        assertEquals(120, second.data.runtimeMinutes)
        assertEquals(listOf(42, 42), api.ids)
    }

    @Test fun invalidIdsAreRejectedWithoutCallingApi() = runTest {
        for (id in listOf(0, -1, Int.MIN_VALUE)) {
            assertEquals(DataResult.Failed(DataError.NOT_FOUND), repository.getMovieDetails(id))
        }
        assertTrue(api.ids.isEmpty())
    }

    @Test fun detailsForDifferentMovieAreRejected() = runTest {
        api.details = { MovieDetailsDto(99, "Wrong") }
        assertEquals(DataResult.Failed(DataError.UNKNOWN), repository.getMovieDetails(42))
        assertEquals(listOf(42), api.ids)
    }

    @Test fun repositoryUsesExecutorToTranslateHttpFailure() = runTest {
        api.details = { throw HttpException(Response.error<Any>(404, "{}".toResponseBody())) }
        assertEquals(DataResult.Failed(DataError.NOT_FOUND), repository.getMovieDetails(42))
    }

    @Test fun cancellationPropagates() = runTest {
        val cancellation = CancellationException("cancelled")
        api.details = { throw cancellation }
        try {
            repository.getMovieDetails(42)
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
            assertEquals(listOf(42), api.ids)
        }
    }

    @Test fun catalogMappingPreservesAllMoviesRegardlessOfShuffledOrder() = runTest {
        api.discover = { page ->
            MoviesResponse(page, listOf(MovieDto(1, "Primeiro"), MovieDto(2, "Segundo"), MovieDto(3, "Terceiro")), 20)
        }
        val result = repository.getMovies() as DataResult.Success<List<Movie>>

        assertEquals(3, result.data.size)
        assertEquals(mapOf(1 to "Primeiro", 2 to "Segundo", 3 to "Terceiro"), result.data.associate { it.id to it.title })
        assertEquals(1, api.pages.size)
    }

    @Test fun failedRefreshDoesNotReturnPreviouslyLoadedCatalog() = runTest {
        repository.getMovies()
        api.discover = { throw IOException("offline") }

        assertEquals(DataResult.Failed(DataError.NETWORK), repository.getMovies())
        assertEquals(2, api.pages.size)
    }

    @Test fun failedRefreshDoesNotReturnPreviouslyLoadedDetails() = runTest {
        repository.getMovieDetails(42)
        api.details = { throw IOException("offline") }

        assertEquals(DataResult.Failed(DataError.NETWORK), repository.getMovieDetails(42))
        assertEquals(listOf(42, 42), api.ids)
    }

    @Test fun malformedDetailsAreTranslatedByExecutor() = runTest {
        api.details = { throw SerializationException("invalid response") }

        assertEquals(DataResult.Failed(DataError.UNKNOWN), repository.getMovieDetails(42))
        assertEquals(listOf(42), api.ids)
    }

    @Test fun catalogCancellationPropagatesWithoutRetry() = runTest {
        val cancellation = CancellationException("cancelled")
        api.discover = { throw cancellation }

        try {
            repository.getMovies()
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
        assertEquals(1, api.pages.size)
    }

    @Test fun unexpectedErrorsPropagateFromBothQueries() = runTest {
        val error = IllegalStateException("unexpected")
        api.discover = { throw error }
        api.details = { throw error }

        try {
            repository.getMovies()
            fail("Unexpected exceptions must propagate")
        } catch (actual: IllegalStateException) {
            assertSame(error, actual)
        }
        try {
            repository.getMovieDetails(42)
            fail("Unexpected exceptions must propagate")
        } catch (actual: IllegalStateException) {
            assertSame(error, actual)
        }
        assertEquals(1, api.pages.size)
        assertEquals(listOf(42), api.ids)
    }

    private class FakeApi : TmdbApi {
        val pages = mutableListOf<Int>()
        val ids = mutableListOf<Int>()
        var discover: suspend (Int) -> MoviesResponse =
            { page -> MoviesResponse(page, listOf(MovieDto(1, "Movie")), 20) }
        var details: suspend (Int) -> MovieDetailsDto =
            { id -> MovieDetailsDto(id, "Details", runtime = 117) }
        override suspend fun getDiscoverMovies(
            language: String, page: Int, includeAdult: Boolean, includeVideo: Boolean, sortBy: String,
        ): MoviesResponse { pages += page; return discover(page) }
        override suspend fun getMovieDetails(movieId: Int, language: String): MovieDetailsDto {
            ids += movieId
            return details(movieId)
        }
    }
}
