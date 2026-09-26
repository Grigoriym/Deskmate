package com.grappim.deskmate.composeapp.discovery

import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.DeskResult
import com.grappim.deskmate.core.discovery.HostProbe
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/**
 * Probes a candidate with `GET /api/status`. One [DeskApi] whose `baseUrl` is the host under
 * test, so each probe does not build (and leak) a new `HttpClient`.
 */
@Single(binds = [HostProbe::class])
internal class DeskHostProbe(engine: HttpClientEngine) : HostProbe {
    private val mutex = Mutex()
    private var host = ""
    private val api = DeskApi(engine) { host }

    override suspend fun isDisplay(host: String): Boolean = mutex.withLock {
        this.host = host
        try {
            api.status() is DeskResult.Success
        } catch (_: IllegalArgumentException) {
            // Not the display: a `200` body that isn't its JSON (`SerializationException`), or a
            // manual address that isn't a valid URL.
            false
        }
    }
}
