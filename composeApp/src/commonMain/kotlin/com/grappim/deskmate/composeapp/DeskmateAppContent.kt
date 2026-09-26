package com.grappim.deskmate.composeapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.DeskResult
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostState
import org.koin.compose.koinInject

// M2.4 temporary proof of discovery — M3 replaces this with the status screen.
@Composable
fun DeskmateAppContent(
    modifier: Modifier = Modifier,
    locator: HostLocator = koinInject(),
    deskApi: DeskApi = koinInject()
) {
    val state by locator.state.collectAsStateWithLifecycle()
    var time by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state) {
        time = null
        if (state is HostState.Found) {
            time = when (val result = deskApi.status()) {
                is DeskResult.Success -> result.value.time
                else -> "status failed: $result"
            }
        }
    }
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val text = when (val s = state) {
                    HostState.Searching -> "Searching for the display…"
                    is HostState.Found -> "Found ${s.host}\nTime: ${time ?: "…"}"
                    HostState.NotFound -> "Display not found"
                }
                Text(text = text, textAlign = TextAlign.Center)
            }
        }
    }
}
