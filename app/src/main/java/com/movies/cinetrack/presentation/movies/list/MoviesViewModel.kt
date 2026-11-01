package com.movies.cinetrack.presentation.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movies.cinetrack.core.diagnostics.Logger
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.usecase.GetMoviesUseCase
import com.movies.cinetrack.presentation.model.UiError
import com.movies.cinetrack.presentation.model.toUiError
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiAction
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiEffect
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMovies: GetMoviesUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading())
    val uiState = mutableUiState.asStateFlow()
    private val effectChannel = Channel<MoviesUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var requestRunning = false
    init { fetch() }

    fun onAction(action: MoviesUiAction) {
        when (action) {
            MoviesUiAction.Refresh, MoviesUiAction.Retry -> fetch()
        }
    }

    private fun fetch() {
        if (requestRunning) return
        val previousMovies = when (val state = uiState.value) {
            is MoviesUiState.Success -> state.movies
            is MoviesUiState.Loading -> state.movies
            is MoviesUiState.Failure -> null
        }
        requestRunning = true
        mutableUiState.value = MoviesUiState.Loading(previousMovies)
        viewModelScope.launch {
            try {
                when (val result = getMovies()) {
                    is DataResult.Success -> mutableUiState.value = MoviesUiState.Success(result.data.toList())
                    is DataResult.Failed -> reportFailure(result.reason.toUiError(), previousMovies)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logger.error("Falha inesperada ao carregar a lista de filmes.", error)
                reportFailure(UiError.UNKNOWN, previousMovies)
            } finally {
                requestRunning = false
            }
        }
    }

    private suspend fun reportFailure(error: UiError, previousMovies: List<Movie>?) {
        if (previousMovies == null) {
            mutableUiState.value = MoviesUiState.Failure(error)
        } else {
            mutableUiState.value = MoviesUiState.Success(previousMovies)
            effectChannel.send(MoviesUiEffect.ShowMessage(error))
        }
    }
}