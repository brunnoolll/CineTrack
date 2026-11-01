package com.movies.cinetrack.presentation.movies.details.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.movies.cinetrack.R
import com.movies.cinetrack.presentation.components.FailureContent
import com.movies.cinetrack.presentation.components.LoadingContent
import com.movies.cinetrack.presentation.model.messageResId
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiAction
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiEffect
import com.movies.cinetrack.presentation.movies.details.contract.MovieDetailsUiState
import com.movies.cinetrack.presentation.movies.details.MovieDetailsViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsRoute(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(viewModel, lifecycleOwner, context, snackbarHostState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is MovieDetailsUiEffect.ShowMessage -> {
                        if (snackbarHostState.currentSnackbarData == null) {
                            launch(start = CoroutineStart.UNDISPATCHED) {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(effect.error.messageResId()),
                                    withDismissAction = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val details = when (val current = state) {
        is MovieDetailsUiState.Success -> current.movie
        is MovieDetailsUiState.Loading -> current.movie
        is MovieDetailsUiState.Failure -> null
    }
    val scrollState = rememberScrollState()
    val refreshLabel = stringResource(R.string.refresh_details)
    PullToRefreshBox(
        isRefreshing = state is MovieDetailsUiState.Loading && details != null,
        onRefresh = { viewModel.onAction(MovieDetailsUiAction.Refresh) },
        modifier = modifier.fillMaxSize().testTag("details_refresh").semantics {
            customActions = if (state is MovieDetailsUiState.Loading) emptyList() else listOf(
                CustomAccessibilityAction(refreshLabel) {
                    viewModel.onAction(MovieDetailsUiAction.Refresh)
                    true
                },
            )
        },
    ) {
        when {
            details != null -> MovieDetailsContent(details, scrollState = scrollState)
            state is MovieDetailsUiState.Failure -> FailureContent(
                message = stringResource((state as MovieDetailsUiState.Failure).error.messageResId()),
                onRetry = { viewModel.onAction(MovieDetailsUiAction.Retry) },
            )
            else -> LoadingContent()
        }
    }
}