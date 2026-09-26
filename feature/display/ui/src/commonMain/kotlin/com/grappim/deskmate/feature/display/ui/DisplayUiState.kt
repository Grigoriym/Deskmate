package com.grappim.deskmate.feature.display.ui

import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.feature.display.domain.DisplayStatus

data class DisplayUiState(
    val host: HostState = HostState.Searching,
    /** The last status that decoded. It stays when a later call fails. `null` before the first one. */
    val status: DisplayStatus? = null,
    /** The display went offline after [status] was read. Clears on the next successful poll. */
    val isStale: Boolean = false,
    /** The display answered, but not with a usable status. Clears on the next successful poll. */
    val error: DisplayError? = null,
    /** The last manual IP did not answer as the display. */
    val manualIpRejected: Boolean = false
)

sealed interface DisplayError {
    /** A non-2xx answer. API.md: [body] is a short `text/html` message. */
    data class Http(val status: Int, val body: String) : DisplayError

    /** A `2xx` answer whose body is not the status JSON this app knows. */
    data object Undecodable : DisplayError
}
