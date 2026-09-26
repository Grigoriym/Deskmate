package com.grappim.deskmate.feature.display.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.DeskResult
import com.grappim.deskmate.core.api.PanelCommand
import com.grappim.deskmate.core.api.ScreenCommand
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.feature.display.domain.toDisplayStatus
import com.grappim.kit.logger.logcat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/** API.md "Client guidelines": every 5 s while visible. */
private const val POLL_INTERVAL_MS = 5_000L

/** API.md "Command responses and timing": the display's own page re-reads 150 ms after a command. */
private const val REREAD_DELAY_MS = 150L

/**
 * The poll loop and the commands for the status screen.
 *
 * A `ViewModel`, not a plain state holder: commands and `rediscover()` need a scope that outlives
 * a recomposition and a rotation, and the last status must survive a rotation too.
 */
@KoinViewModel
class DisplayViewModel(private val locator: HostLocator, private val api: DeskApi) : ViewModel() {
    private val content = MutableStateFlow(DisplayUiState())
    private var rediscovery: Job? = null

    /**
     * Polls only while this has a collector, and only in [HostState.Found]. The screen collects
     * it with `collectAsStateWithLifecycle`, so polling stops as soon as the screen is not visible.
     */
    val uiState: StateFlow<DisplayUiState> = merge(
        combine(locator.state, content) { host, data -> data.copy(host = host) },
        polling()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DisplayUiState(host = locator.state.value))

    fun screen(go: ScreenCommand) = command { screen(go) }

    fun panel(set: PanelCommand) = command { panel(set) }

    /**
     * The retry in the not-found state. `locate()`, not `rediscover()`: the saved host can be a
     * manual IP that NSD and `desk.local` never find.
     */
    fun retry() {
        viewModelScope.launch { locator.locate() }
    }

    /** [ip] without a scheme, for example `192.168.0.147`. A rejected IP shows in [uiState]. */
    fun setManual(ip: String) {
        viewModelScope.launch {
            val found = locator.setManual(ip)
            content.update { it.copy(manualIpRejected = !found) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun polling(): Flow<Nothing> = locator.state.transformLatest { host ->
        if (host is HostState.Found) {
            while (true) {
                poll()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun poll() {
        when (val result = call { status() }) {
            null -> Unit

            is DeskResult.Success -> content.update {
                it.copy(status = result.value.toDisplayStatus(), isStale = false, error = null)
            }

            else -> onFailure(result)
        }
    }

    /**
     * API.md: `{"ok":true}` means "queued". Re-read the status shortly after; the normal poll
     * shows the change if the display needs longer (up to ~10 s), so that delay is no failure.
     */
    private fun command(send: suspend DeskApi.() -> DeskResult<Unit>) {
        viewModelScope.launch {
            when (val result = call(send)) {
                null -> Unit

                is DeskResult.Success -> {
                    delay(REREAD_DELAY_MS)
                    poll()
                }

                else -> onFailure(result)
            }
        }
    }

    private fun onFailure(result: DeskResult<*>) {
        when (result) {
            is DeskResult.Success -> Unit

            // The display answered, so its address is right: no rediscover.
            is DeskResult.HttpError -> content.update { it.copy(error = DisplayError.Http(result.status, result.body)) }

            is DeskResult.Undecodable -> {
                logcat { "Status body did not decode: ${result.cause.message}" }
                content.update { it.copy(error = DisplayError.Undecodable) }
            }

            is DeskResult.Offline -> {
                content.update { it.copy(isStale = true) }
                rediscoverOnce()
            }
        }
    }

    /**
     * `rediscover()` leaves [HostState.Found], which stops the poll loop, so it runs once per
     * offline spell. The check is for a command that fails at the same time as a poll.
     */
    private fun rediscoverOnce() {
        if (rediscovery?.isActive == true) return
        rediscovery = viewModelScope.launch { locator.rediscover() }
    }

    /**
     * `null` when the call did not run. [DeskApi] throws `IllegalStateException` outside
     * [HostState.Found]. The state can change while a call waits for [DeskApi]'s lock, so the
     * check before the call is not enough on its own.
     */
    private suspend fun <T> call(block: suspend DeskApi.() -> DeskResult<T>): DeskResult<T>? {
        if (locator.state.value !is HostState.Found) return null
        return try {
            api.block()
        } catch (e: IllegalStateException) {
            // `CancellationException` is an `IllegalStateException` too.
            if (e is CancellationException) throw e
            logcat { "Call skipped: ${e.message}" }
            null
        }
    }
}
