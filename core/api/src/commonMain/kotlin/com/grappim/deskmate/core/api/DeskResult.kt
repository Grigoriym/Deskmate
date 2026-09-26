package com.grappim.deskmate.core.api

/** The outcome of one call to the display. */
sealed interface DeskResult<out T> {
    data class Success<T>(val value: T) : DeskResult<T>

    /** The display answered with a non-2xx status. API.md: the body is a short `text/html` message. */
    data class HttpError(val status: Int, val body: String) : DeskResult<Nothing>

    /**
     * No answer: timeout, connection refused, unknown host. API.md: treat this as "display
     * offline" (it reboots after a flash or a power blip, and is gone when unplugged).
     */
    data class Offline(val cause: Throwable) : DeskResult<Nothing>
}
