package com.movies.cinetrack.domain.usecase

import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class MovieUseCasesTest {
    private val repository = FakeMovieRepository()
    private val getMovies = GetMoviesUseCase(repository)
    private val getDetails = GetMovieDetailsUseCase(repository)
    private val movie = Movie(42, "Filme", "Sinopse", null, null, 7.8, null)

    @Test fun catalogReturnsRepositoryResultAndConsultsAgainOnNextCall() = runTest {
        repository.moviesResponse = { DataResult.Success(listOf(movie)) }
        assertEquals(DataResult.Success(listOf(movie)), getMovies())

        repository.moviesResponse = { DataResult.Success(emptyList()) }
        assertEquals(DataResult.Success(emptyList<Movie>()), getMovies())
        assertEquals(2, repository.catalogCalls)
        assertEquals(emptyList<Int>(), repository.detailsIds)
    }

    @Test fun detailsPassesSelectedIdAndReturnsCurrentRepositoryResult() = runTest {
        val details = MovieDetails(movie, 117, listOf("Drama"))
        repository.detailsResponse = { DataResult.Success(details) }
        assertEquals(DataResult.Success(details), getDetails(42))

        val other = details.copy(movie = movie.copy(id = 99), runtimeMinutes = 120)
        repository.detailsResponse = { DataResult.Success(other) }
        assertEquals(DataResult.Success(other), getDetails(99))
        assertEquals(listOf(42, 99), repository.detailsIds)
        assertEquals(0, repository.catalogCalls)
    }

    @Test fun catalogPreservesAllDomainFailureCategoriesWithoutRetry() = runTest {
        for (error in DataError.entries) {
            repository.moviesResponse = { DataResult.Failed(error) }
            assertEquals(DataResult.Failed(error), getMovies())
        }
        assertEquals(DataError.entries.size, repository.catalogCalls)
    }

    @Test fun detailsPreservesAllDomainFailureCategoriesWithoutRetry() = runTest {
        for (error in DataError.entries) {
            repository.detailsResponse = { DataResult.Failed(error) }
            assertEquals(DataResult.Failed(error), getDetails(42))
        }
        assertEquals(List(DataError.entries.size) { 42 }, repository.detailsIds)
    }

    @Test fun catalogPropagatesCancellationAndUnexpectedExceptionsUnchanged() = runTest {
        for (error in listOf(CancellationException("cancelled"), IllegalStateException("unexpected"))) {
            repository.moviesResponse = { throw error }
            try {
                getMovies()
                fail("A exceção deve ser propagada")
            } catch (actual: Exception) {
                assertSame(error, actual)
            }
        }
        assertEquals(2, repository.catalogCalls)
    }

    @Test fun detailsPropagatesCancellationAndUnexpectedExceptionsUnchanged() = runTest {
        for (error in listOf(CancellationException("cancelled"), IllegalStateException("unexpected"))) {
            repository.detailsResponse = { throw error }
            try {
                getDetails(42)
                fail("A exceção deve ser propagada")
            } catch (actual: Exception) {
                assertSame(error, actual)
            }
        }
        assertEquals(listOf(42, 42), repository.detailsIds)
    }

    private class FakeMovieRepository : MovieRepository {
        var catalogCalls = 0
        val detailsIds = mutableListOf<Int>()
        var moviesResponse: suspend () -> DataResult<List<Movie>> = { error("Resposta não configurada") }
        var detailsResponse: suspend () -> DataResult<MovieDetails> = { error("Resposta não configurada") }

        override suspend fun getMovies(): DataResult<List<Movie>> {
            catalogCalls++
            return moviesResponse()
        }

        override suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails> {
            detailsIds += movieId
            return detailsResponse()
        }
    }
}
