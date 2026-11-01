package com.movies.cinetrack.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.movies.cinetrack.ui.theme.CineTrackTheme
import com.movies.cinetrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineTrackTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
        actions = actions,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_back), stringResource(R.string.go_back))
                }
            }
        },
    )
}

@Preview(name = "Top bar - Home", showBackground = true, widthDp = 360)
@Composable
private fun CineTrackHomeTopBarPreview() {
    CineTrackTheme { CineTrackTopBar(title = stringResource(R.string.app_name)) }
}

@Preview(name = "Top bar - Detalhes", showBackground = true, widthDp = 360)
@Composable
private fun CineTrackDetailsTopBarPreview() {
    CineTrackTheme { CineTrackTopBar(title = stringResource(R.string.movie_details), onBack = {}) }
}