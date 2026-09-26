package com.grappim.deskmate.composeapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// M0.2 placeholder — M3 replaces this with the status screen.
@Composable
fun DeskmateAppContent(modifier: Modifier = Modifier) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Deskmate")
            }
        }
    }
}
