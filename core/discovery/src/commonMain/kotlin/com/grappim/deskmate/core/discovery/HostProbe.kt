package com.grappim.deskmate.core.discovery

/**
 * Checks one candidate host: `true` only when `GET <host>/api/status` returns
 * `DeskResult.Success`.
 *
 * An interface, not a `core:api` dependency: [HostLocator] probes a different host on each
 * call, but `DeskApi` is built around one `baseUrl` and one HTTP engine. The app module
 * implements this with `DeskApi` (M2.4), and tests use a fake.
 */
fun interface HostProbe {
    suspend fun isDisplay(host: String): Boolean
}
