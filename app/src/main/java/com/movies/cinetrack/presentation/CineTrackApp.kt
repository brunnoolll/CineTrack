package com.movies.cinetrack.presentation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.movies.cinetrack.R
import com.movies.cinetrack.presentation.components.CineTrackTopBar
import com.movies.cinetrack.presentation.navigation.CineTrackDestination
import com.movies.cinetrack.presentation.navigation.CineTrackNavHost

@Composable
fun CineTrackApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isDetails = backStackEntry?.destination
        ?.hasRoute<CineTrackDestination.MovieDetails>() == true
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CineTrackTopBar(
                title = stringResource(if (isDetails) R.string.movie_details else R.string.app_name),
                onBack = if (isDetails) ({ navController.popBackStack(); Unit }) else null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        CineTrackNavHost(
            navController = navController,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        )
    }
}