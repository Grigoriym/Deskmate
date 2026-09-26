package com.grappim.deskmate.composeapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.grappim.deskmate.composeapp.greeting.GreetingProvider
import org.koin.compose.koinInject

// M0.2 placeholder — M3 replaces this with the status screen.
@Composable
fun DeskmateAppContent(modifier: Modifier = Modifier, greetingProvider: GreetingProvider = koinInject()) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = greetingProvider.greeting())
            }
        }
    }
}
