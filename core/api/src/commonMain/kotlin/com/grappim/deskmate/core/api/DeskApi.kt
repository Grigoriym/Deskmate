package com.grappim.deskmate.core.api

import com.grappim.deskmate.core.api.dto.StatusDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

private const val CONNECT_TIMEOUT_MS = 3_000L
private const val REQUEST_TIMEOUT_MS = 5_000L

/**
 * The display's HTTP API (`../esp32-desk-display/docs/API.md`).
 *
 * API.md: the server handles one request at a time, so calls here never overlap; a second
 * call waits until the first one ends. It also asks for short timeouts (~3-5 s).
 *
 * [baseUrl] is read on every call, e.g. `http://192.168.0.147`: the display's address comes
 * from DHCP and can change between calls.
 */
class DeskApi(engine: HttpClientEngine, private val baseUrl: () -> String) {
    private val client = HttpClient(engine) {
        // Non-2xx statuses become DeskResult.HttpError, not exceptions.
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }
    }

    private val mutex = Mutex()

    suspend fun status(): DeskResult<StatusDto> = call({ client.get("${baseUrl()}/api/status") }) {
        DeskJson.decodeFromString(StatusDto.serializer(), it)
    }

    /** `{"ok":true}` only means "queued"; re-read [status] to see the change (API.md). */
    suspend fun screen(go: ScreenCommand): DeskResult<Unit> =
        call({ client.post("${baseUrl()}/api/screen?go=${go.wire}") }) {}

    /** `{"ok":true}` only means "queued"; re-read [status] to see the change (API.md). */
    suspend fun panel(set: PanelCommand): DeskResult<Unit> =
        call({ client.post("${baseUrl()}/api/panel?set=${set.wire}") }) {}

    private suspend fun <T> call(request: suspend () -> HttpResponse, parse: (String) -> T): DeskResult<T> =
        mutex.withLock {
            try {
                val response = request()
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    DeskResult.Success(parse(body))
                } else {
                    DeskResult.HttpError(response.status.value, body)
                }
            } catch (e: SerializationException) {
                DeskResult.Undecodable(e)
            } catch (e: IOException) {
                // Ktor's timeout exceptions are IOExceptions too, next to refused and unknown host.
                DeskResult.Offline(e)
            }
        }
}
