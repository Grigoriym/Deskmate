package com.grappim.deskmate.composeapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grappim.deskmate.feature.display.ui.DisplayScreen

@Composable
fun DeskmateAppContent(modifier: Modifier = Modifier) {
    MaterialTheme {
        DisplayScreen(modifier = modifier)
    }
}
