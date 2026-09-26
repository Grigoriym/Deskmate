package com.grappim.deskmate.core.discovery

import com.grappim.kit.logger.logcat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/** API.md "Finding the display": the `.local` name, for when NSD finds nothing. */
private const val MDNS_HOST = "http://desk.local"

/**
 * Finds the display. Search order: saved host, then NSD, then `desk.local`. The first
 * candidate that passes [HostProbe] is saved and becomes [HostState.Found].
 *
 * Nothing searches until a caller runs [locate]. One search runs at a time; a second call
 * waits until the first one ends.
 */
@Single
class HostLocator(private val store: SavedHostStore, private val finder: DisplayFinder, private val probe: HostProbe) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow<HostState>(HostState.Searching)
    val state: StateFlow<HostState> = _state.asStateFlow()

    suspend fun locate() = search(trySaved = true)

    /** For when the found host goes offline: search again, without the saved host. */
    suspend fun rediscover() = search(trySaved = false)

    /**
     * [ip] is an address without a scheme, for example `192.168.0.147`. It is saved only
     * when it passes [HostProbe]. On `false` the state does not change.
     */
    suspend fun setManual(ip: String): Boolean = mutex.withLock {
        val host = "http://${ip.trim()}"
        if (!probe.isDisplay(host)) return@withLock false
        store.save(host)
        logcat { "Found $host via manual IP" }
        _state.value = HostState.Found(host)
        true
    }

    private suspend fun search(trySaved: Boolean) = mutex.withLock {
        _state.value = HostState.Searching
        val saved = if (trySaved) store.read() else null
        // Each candidate is computed only when the one before it failed: NSD takes seconds.
        val candidates = listOf<Pair<String, suspend () -> String?>>(
            "saved host" to { saved },
            "NSD" to { finder.find() },
            "desk.local" to { MDNS_HOST }
        )
        for ((path, candidate) in candidates) {
            val host = candidate() ?: continue
            if (probe.isDisplay(host)) {
                store.save(host)
                logcat { "Found $host via $path" }
                _state.value = HostState.Found(host)
                return@withLock
            }
            logcat { "No display at $host ($path)" }
        }
        logcat { "Display not found" }
        _state.value = HostState.NotFound
    }
}
