package com.movies.cinetrack.presentation.movies.list.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.movies.cinetrack.presentation.components.LoadingContent
import com.movies.cinetrack.presentation.components.FailureContent
import com.movies.cinetrack.R
import com.movies.cinetrack.presentation.model.messageResId
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiAction
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiEffect
import com.movies.cinetrack.presentation.movies.list.contract.MoviesUiState
import com.movies.cinetrack.presentation.movies.list.MoviesViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesRoute(
    onMovieSelected: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(viewModel, lifecycleOwner, context, snackbarHostState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is MoviesUiEffect.ShowMessage -> {
                        if (snackbarHostState.currentSnackbarData == null) {
                            launch(start = CoroutineStart.UNDISPATCHED) {
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(effect.error.messageResId()),
                                    actionLabel = context.getString(R.string.retry),
                                    withDismissAction = true,
                                )
                                if (result == SnackbarResult.ActionPerformed) viewModel.onAction(
                                    MoviesUiAction.Retry)
                            }
                        }
                    }
                }
            }
        }
    }
    val listState = rememberLazyListState()

    val uiState = state
    val movies = when (uiState) {
        is MoviesUiState.Success -> uiState.movies
        is MoviesUiState.Loading -> uiState.movies
        is MoviesUiState.Failure -> null
    }

    val refreshLabel = stringResource(R.string.refresh_movies)
    PullToRefreshBox(
        isRefreshing = uiState is MoviesUiState.Loading && movies != null,
        onRefresh = { viewModel.onAction(MoviesUiAction.Refresh) },
        modifier = modifier.fillMaxSize().testTag("movies_refresh").semantics {
            customActions = if (uiState is MoviesUiState.Loading) emptyList() else listOf(
                CustomAccessibilityAction(refreshLabel) { viewModel.onAction(MoviesUiAction.Refresh); true },
            )
        },
    ) {
        when {
            movies != null -> MoviesSuccessContent(
                movies = movies,
                onMovieSelected = onMovieSelected,
                listState = listState,
                bottomContentPadding = bottomContentPadding,
            )
            uiState is MoviesUiState.Failure -> FailureContent(
                message = stringResource(uiState.error.messageResId()),
                onRetry = { viewModel.onAction(MoviesUiAction.Retry) },
            )
            else -> LoadingContent()
        }
    }
}