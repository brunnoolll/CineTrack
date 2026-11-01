package com.movies.cinetrack.presentation.movies.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.movies.cinetrack.core.diagnostics.TestLogger
import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.usecase.GetMovieDetailsUseCase
import com.movies.cinetrack.presentation.MainDispatcherRule
import com.movies.cinetrack.presentation.OnlineRepositoryFake
import com.movies.cinetrack.presentation.model.UiError
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiAction
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiEffect
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiState
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
class MovieDetailsViewModelTest {
    @get:Rule internal val dispatcher = MainDispatcherRule()
    private val store = ViewModelStore()
    private val repository = OnlineRepositoryFake()
    private val reports = mutableListOf<Pair<String, Throwable?>>()

    @After fun tearDown() = store.clear()

    private fun create(movieId: Int? = 42): MovieDetailsViewModel = MovieDetailsViewModel(
        getMovieDetails = GetMovieDetailsUseCase(repository),
        savedStateHandle = SavedStateHandle(if (movieId != null) mapOf("movieId" to movieId) else emptyMap()),
        logger = TestLogger { message, cause -> reports.add(message to cause) },
    ).also { store.put("details", it) }

    @Test fun loadsDetailsDirectlyFromApi() = runTest {
        val pending = CompletableDeferred<DataResult<MovieDetails>>()
        repository.detailsResponse = { pending.await() }
        val viewModel = create()
        runCurrent()
        assertEquals(MovieDetailsUiState.Loading(), viewModel.uiState.value)
        assertEquals(listOf(42), repository.ids)
        pending.complete(DataResult.Success(repository.details))
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)
    }

    @Test fun refreshAndRetryDuringInitialLoadDoNotDuplicateRequest() = runTest {
        val pending = CompletableDeferred<DataResult<MovieDetails>>()
        repository.detailsResponse = { pending.await() }
        val viewModel = create()
        runCurrent()

        assertEquals(listOf(42), repository.ids)
        assertEquals(MovieDetailsUiState.Loading(), viewModel.uiState.value)

        viewModel.onAction(MovieDetailsUiAction.Refresh)
        viewModel.onAction(MovieDetailsUiAction.Retry)
        runCurrent()

        assertEquals(listOf(42), repository.ids)
        pending.complete(DataResult.Success(repository.details))
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)
    }

    @Test fun invalidIdFailsWithoutRequest() = runTest {
        val viewModel = create(0)
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Failure(UiError.NOT_FOUND), viewModel.uiState.value)
        assertTrue(repository.ids.isEmpty())
    }

    @Test fun missingIdFailsWithoutRequest() = runTest {
        val viewModel = create(null)
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Failure(UiError.NOT_FOUND), viewModel.uiState.value)
        assertTrue(repository.ids.isEmpty())
    }

    @Test fun networkFailureCanBeRetried() = runTest {
        repository.detailsResponse = { DataResult.Failed(DataError.NETWORK) }
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Failure(UiError.NETWORK), viewModel.uiState.value)
        repository.detailsResponse = { DataResult.Success(repository.details) }
        viewModel.onAction(MovieDetailsUiAction.Retry)
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)
        assertEquals(listOf(42, 42), repository.ids)
    }

    @Test fun refreshPreservesCurrentMovieWhileLoading() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        val pending = CompletableDeferred<DataResult<MovieDetails>>()
        repository.detailsResponse = { pending.await() }
        repeat(3) { viewModel.onAction(MovieDetailsUiAction.Refresh) }
        runCurrent()
        assertEquals(MovieDetailsUiState.Loading(repository.details), viewModel.uiState.value)
        assertEquals(2, repository.ids.size)
        pending.complete(DataResult.Success(repository.details.copy(runtimeMinutes = 140)))
        advanceUntilIdle()
        assertEquals(140, (viewModel.uiState.value as MovieDetailsUiState.Success).movie.runtimeMinutes)
    }

    @Test fun refreshFailureKeepsContentAndEmitsMessage() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        repository.detailsResponse = { DataResult.Failed(DataError.NETWORK) }
        viewModel.onAction(MovieDetailsUiAction.Refresh)
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)
        assertEquals(MovieDetailsUiEffect.ShowMessage(UiError.NETWORK), withTimeout(1_000) { viewModel.effects.first() })
    }

    @Test fun unexpectedFailureDuringRefreshKeepsCurrentMovieAndEmitsMessage() = runTest {
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)

        val pending = CompletableDeferred<DataResult<MovieDetails>>()
        repository.detailsResponse = { pending.await() }
        val error = IllegalStateException("unexpected refresh failure")

        viewModel.onAction(MovieDetailsUiAction.Refresh)
        runCurrent()

        assertEquals(MovieDetailsUiState.Loading(repository.details), viewModel.uiState.value)
        assertEquals(listOf(42, 42), repository.ids)

        pending.completeExceptionally(error)
        advanceUntilIdle()

        assertEquals(MovieDetailsUiState.Success(repository.details), viewModel.uiState.value)
        assertEquals(
            MovieDetailsUiEffect.ShowMessage(UiError.UNKNOWN),
            withTimeout(1_000) { viewModel.effects.first() },
        )
        assertEquals(1, reports.size)
        val reportedError = reports.single().second
        assertTrue(reportedError is IllegalStateException)
        assertEquals(error.message, reportedError?.message)
    }

    @Test fun unexpectedFailureIsReportedAsUnknown() = runTest {
        val error = IllegalStateException("unexpected")
        repository.detailsResponse = { throw error }
        val viewModel = create()
        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Failure(UiError.UNKNOWN), viewModel.uiState.value)
        assertEquals(1, reports.size)
        assertSame(error, reports.single().second)
    }

    @Test fun newViewModelRequestsDetailsAgain() = runTest {
        create(); advanceUntilIdle()
        create(); advanceUntilIdle()
        assertEquals(listOf(42, 42), repository.ids)
    }
}