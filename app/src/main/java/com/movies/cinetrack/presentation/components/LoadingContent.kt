package com.movies.cinetrack.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.movies.cinetrack.ui.theme.CineTrackTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.testTag("loading_indicator"))
    }
}

@Preview(name = "LoadingContent", showBackground = true, backgroundColor = 0xFF0E0F13, widthDp = 360, heightDp = 640)
@Composable
private fun LoadingContentPreview() {
    CineTrackTheme {
        LoadingContent()
    }
}