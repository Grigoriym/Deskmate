package com.grappim.deskmate.core.discovery

sealed interface HostState {
    data object Searching : HostState

    /** [host] is in the form `DeskApi.baseUrl` takes, for example `http://192.168.0.147`. */
    data class Found(val host: String) : HostState

    /** No candidate answered. The saved host stays in the store for the next search. */
    data object NotFound : HostState
}
