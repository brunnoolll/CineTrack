package com.movies.cinetrack.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movies.cinetrack.presentation.movies.details.ui.MovieDetailsRoute
import com.movies.cinetrack.presentation.movies.list.ui.MoviesRoute

@Composable
fun CineTrackNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(navController, startDestination = CineTrackDestination.Home, modifier = modifier) {
        composable<CineTrackDestination.Home> {
            MoviesRoute(
                snackbarHostState = snackbarHostState,
                onMovieSelected = { movieId ->
                    navController.navigate(CineTrackDestination.MovieDetails(movieId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<CineTrackDestination.MovieDetails> {
            MovieDetailsRoute(snackbarHostState = snackbarHostState)
        }
    }
}