package com.movies.cinetrack.presentation.movies.list

import com.movies.cinetrack.core.diagnostics.TestLogger

import androidx.lifecycle.ViewModelStore
import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.usecase.GetMoviesUseCase
import com.movies.cinetrack.presentation.MainDispatcherRule
import com.movies.cinetrack.presentation.OnlineRepositoryFake
import com.movies.cinetrack.presentation.model.UiError
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiAction
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiEffect
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {
    @get:Rule internal val dispatcher = MainDispatcherRule()
    private val store = ViewModelStore()
    private val repository = OnlineRepositoryFake()
    private val reports = mutableListOf<Pair<String, Throwable?>>()
    @After fun tearDown() { store.clear() }

    @Test fun initialLoadWaitsForApiThenShowsSuccess() = runTest {
        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }
        val viewModel = create()
        runCurrent()
        assertEquals(MoviesUiState.Loading(), viewModel.uiState.value)
        assertEquals(1, repository.calls)
        response.complete(DataResult.Success(listOf(repository.movie)))
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(listOf(repository.movie)), viewModel.uiState.value)
    }

    @Test fun refreshAndRetryWhileInitialLoadIsRunningDoNotStartAnotherRequest() = runTest {
        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }
        val viewModel = create()
        runCurrent()
        assertEquals(1, repository.calls)

        repeat(3) {
            viewModel.onAction(MoviesUiAction.Refresh)
            viewModel.onAction(MoviesUiAction.Retry)
        }
        runCurrent()
        assertEquals(1, repository.calls)
        assertEquals(MoviesUiState.Loading(), viewModel.uiState.value)

        response.complete(DataResult.Success(listOf(repository.movie)))
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(listOf(repository.movie)), viewModel.uiState.value)
        assertEquals(1, repository.calls)
    }

    @Test fun initialFailureCanBeRetried() = runTest {
        repository.moviesResponse = { DataResult.Failed(DataError.NETWORK) }
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MoviesUiState.Failure(UiError.NETWORK), viewModel.uiState.value)
        repository.moviesResponse = { DataResult.Success(listOf(repository.movie)) }
        viewModel.onAction(MoviesUiAction.Retry)
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(listOf(repository.movie)), viewModel.uiState.value)
        assertEquals(2, repository.calls)
    }

    @Test fun refreshFailureRestoresContentAndEmitsMessageEffect() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        repository.moviesResponse = { DataResult.Failed(DataError.NETWORK) }
        viewModel.onAction(MoviesUiAction.Refresh)
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(listOf(repository.movie)), viewModel.uiState.value)
        val effect = withTimeout(1_000) { viewModel.effects.first() }
        assertEquals(MoviesUiEffect.ShowMessage(UiError.NETWORK), effect)
    }

    @Test fun refreshRetainsContentAndRepeatedActionsDoNotDuplicateRequest() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }
        repeat(5) {
            viewModel.onAction(MoviesUiAction.Refresh)
            viewModel.onAction(MoviesUiAction.Retry)
        }
        runCurrent()
        assertEquals(2, repository.calls)
        assertEquals(MoviesUiState.Loading(listOf(repository.movie)), viewModel.uiState.value)
        response.complete(DataResult.Success(listOf(repository.movie)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is MoviesUiState.Success)
    }

    @Test fun refreshReplacesPreviousMoviesWithNewApiResult() = runTest {
        val previousMovies = listOf(repository.movie)
        val newMovies = listOf(
            repository.movie.copy(id = 101, title = "Novo filme A"),
            repository.movie.copy(id = 102, title = "Novo filme B"),
        )
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(previousMovies), viewModel.uiState.value)

        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }

        viewModel.onAction(MoviesUiAction.Refresh)
        runCurrent()
        assertEquals(MoviesUiState.Loading(previousMovies), viewModel.uiState.value)

        response.complete(DataResult.Success(newMovies))
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(newMovies), viewModel.uiState.value)
        assertEquals(2, repository.calls)
    }

    @Test fun newViewModelConsultsApiAgain() = runTest {
        create()
        advanceUntilIdle()
        create()
        advanceUntilIdle()
        assertEquals(2, repository.calls)
    }

    @Test fun emptyResponseIsSuccessWithEmptyList() = runTest {
        repository.moviesResponse = { DataResult.Success(emptyList()) }
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(emptyList()), viewModel.uiState.value)
    }

    @Test fun refreshFailurePreservesEmptyResultAndEmitsMessageEffect() = runTest {
        repository.moviesResponse = { DataResult.Success(emptyList()) }
        val viewModel = create()
        advanceUntilIdle()
        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }

        viewModel.onAction(MoviesUiAction.Refresh)
        runCurrent()
        assertEquals(MoviesUiState.Loading(emptyList()), viewModel.uiState.value)

        response.complete(DataResult.Failed(DataError.NETWORK))
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(emptyList()), viewModel.uiState.value)
        assertEquals(
            MoviesUiEffect.ShowMessage(UiError.NETWORK),
            withTimeout(1_000) { viewModel.effects.first() },
        )
        assertEquals(2, repository.calls)
    }

    @Test fun refreshAfterEmptyResultCanShowMovies() = runTest {
        repository.moviesResponse = { DataResult.Success(emptyList()) }
        val viewModel = create()
        advanceUntilIdle()
        val response = CompletableDeferred<DataResult<List<Movie>>>()
        repository.moviesResponse = { response.await() }

        viewModel.onAction(MoviesUiAction.Refresh)
        runCurrent()
        assertEquals(MoviesUiState.Loading(emptyList()), viewModel.uiState.value)

        response.complete(DataResult.Success(listOf(repository.movie)))
        advanceUntilIdle()
        assertEquals(MoviesUiState.Success(listOf(repository.movie)), viewModel.uiState.value)
        assertEquals(2, repository.calls)
    }

    private fun create(): MoviesViewModel =
        MoviesViewModel(GetMoviesUseCase(repository), TestLogger { message, cause -> reports.add(message to cause) }).also { store.put("screen", it) }

    @Test fun unexpectedFailureIsReportedWithOriginalCause() = runTest {
        val error = IllegalStateException("unexpected")
        repository.moviesResponse = { throw error }
        val viewModel = create()
        advanceUntilIdle()

        assertEquals(MoviesUiState.Failure(UiError.UNKNOWN), viewModel.uiState.value)
        assertEquals(listOf(("Falha inesperada ao carregar a lista de filmes." to error)), reports)
        assertSame(error, reports.single().second)
    }

    @Test fun knownFailureIsNotReportedAgainByViewModel() = runTest {
        repository.moviesResponse = { DataResult.Failed(DataError.UNAUTHORIZED) }
        val viewModel = create()
        advanceUntilIdle()

        assertEquals(MoviesUiState.Failure(UiError.UNAUTHORIZED), viewModel.uiState.value)
        assertTrue(reports.isEmpty())
    }
}