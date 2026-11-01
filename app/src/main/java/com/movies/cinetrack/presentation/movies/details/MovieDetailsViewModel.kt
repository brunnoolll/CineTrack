package com.movies.cinetrack.presentation.movies.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.cinetrack.core.diagnostics.Logger
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.usecase.GetMovieDetailsUseCase
import com.movies.cinetrack.presentation.model.UiError
import com.movies.cinetrack.presentation.model.toUiError
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiAction
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiEffect
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val getMovieDetails: GetMovieDetailsUseCase,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger,
) : ViewModel() {
    private val movieId = savedStateHandle.get<Int>(MOVIE_ID_ARGUMENT)?.takeIf { it > 0 }
    private val mutableUiState = MutableStateFlow<MovieDetailsUiState>(
        if (movieId == null) MovieDetailsUiState.Failure(UiError.NOT_FOUND)
        else MovieDetailsUiState.Loading(),
    )
    val uiState = mutableUiState.asStateFlow()

    private val effectChannel = Channel<MovieDetailsUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var requestRunning = false

    init { fetch() }

    fun onAction(action: MovieDetailsUiAction) {
        when (action) {
            MovieDetailsUiAction.Refresh -> fetch()
            MovieDetailsUiAction.Retry -> if (uiState.value is MovieDetailsUiState.Failure) fetch()
        }
    }

    private fun fetch() {
        val id = movieId ?: return
        if (requestRunning) return
        val previous = when (val state = uiState.value) {
            is MovieDetailsUiState.Success -> state.movie
            is MovieDetailsUiState.Loading -> state.movie
            is MovieDetailsUiState.Failure -> null
        }
        requestRunning = true
        mutableUiState.value = MovieDetailsUiState.Loading(previous)

        viewModelScope.launch {
            try {
                when (val result = getMovieDetails(id)) {
                    is DataResult.Success -> mutableUiState.value = MovieDetailsUiState.Success(result.data)
                    is DataResult.Failed -> showFailure(result.reason.toUiError(), previous)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logger.error("Falha inesperada ao carregar os detalhes do filme com ID $id.", error)
                showFailure(UiError.UNKNOWN, previous)
            } finally {
                requestRunning = false
            }
        }
    }

    private suspend fun showFailure(error: UiError, previous: MovieDetails?) {
        if (previous == null) {
            mutableUiState.value = MovieDetailsUiState.Failure(error)
        } else {
            mutableUiState.value = MovieDetailsUiState.Success(previous)
            effectChannel.send(MovieDetailsUiEffect.ShowMessage(error))
        }
    }

    companion object { const val MOVIE_ID_ARGUMENT = "movieId" }
}