package com.grappim.deskmate.core.api

import com.grappim.deskmate.core.api.dto.StatusDto
import com.grappim.deskmate.core.api.dto.statusExampleJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeskApiTest {

    @Test
    fun `status gets the status path and decodes the body`() = runTest {
        var request: HttpRequestData? = null
        val api = api {
            request = it
            respond(statusExampleJson)
        }

        val result = api.status()

        assertEquals(HttpMethod.Get, request?.method)
        assertEquals("http://desk.test/api/status", request?.url.toString())
        assertEquals(
            DeskJson.decodeFromString<StatusDto>(statusExampleJson),
            assertIs<DeskResult.Success<StatusDto>>(result).value
        )
    }

    @Test
    fun `screen posts the wire value of every command`() = runTest {
        val urls = mutableListOf<String>()
        val api = api {
            urls += it.url.toString()
            assertEquals(HttpMethod.Post, it.method)
            respond(OK_BODY)
        }

        ScreenCommand.entries.forEach { assertEquals(DeskResult.Success(Unit), api.screen(it)) }

        assertEquals(
            listOf("next", "prev", "home", "outdoor", "air", "indoor", "bvg").map {
                "http://desk.test/api/screen?go=$it"
            },
            urls
        )
    }

    @Test
    fun `panel posts the wire value of every command`() = runTest {
        val urls = mutableListOf<String>()
        val api = api {
            urls += it.url.toString()
            assertEquals(HttpMethod.Post, it.method)
            respond(OK_BODY)
        }

        PanelCommand.entries.forEach { assertEquals(DeskResult.Success(Unit), api.panel(it)) }

        assertEquals(listOf("on", "off", "toggle").map { "http://desk.test/api/panel?set=$it" }, urls)
    }

    @Test
    fun `the base url is read on every call`() = runTest {
        var host = "http://10.0.0.1"
        val urls = mutableListOf<String>()
        val api = api(baseUrl = { host }) {
            urls += it.url.toString()
            respond(OK_BODY)
        }

        api.panel(PanelCommand.Toggle)
        host = "http://10.0.0.2"
        api.panel(PanelCommand.Toggle)

        assertEquals(listOf("http://10.0.0.1/api/panel?set=toggle", "http://10.0.0.2/api/panel?set=toggle"), urls)
    }

    @Test
    fun `a 400 is an http error with its body`() = runTest {
        val api = api { respond("Unknown go value", HttpStatusCode.BadRequest) }

        assertEquals(DeskResult.HttpError(400, "Unknown go value"), api.screen(ScreenCommand.Air))
    }

    @Test
    fun `a 405 is an http error with its body`() = runTest {
        val api = api { respond("Method not allowed", HttpStatusCode.MethodNotAllowed) }

        assertEquals(DeskResult.HttpError(405, "Method not allowed"), api.panel(PanelCommand.On))
    }

    @Test
    fun `a 500 is an http error with its body`() = runTest {
        val api = api { respond("Command queue full", HttpStatusCode.InternalServerError) }

        assertEquals(DeskResult.HttpError(500, "Command queue full"), api.status())
    }

    @Test
    fun `a 200 whose body does not decode is undecodable`() = runTest {
        val api = api { respond("<html>captive portal</html>") }

        assertIs<DeskResult.Undecodable>(api.status())
    }

    @Test
    fun `no answer within the timeout is offline`() = runTest {
        val api = api {
            delay(60_000)
            respond(OK_BODY)
        }

        val result = api.status()

        assertIs<HttpRequestTimeoutException>(assertIs<DeskResult.Offline>(result).cause)
        assertTrue(testScheduler.currentTime <= 5_000, "timed out after ${testScheduler.currentTime} ms")
    }

    /** Refused and unknown host reach common code as IOExceptions from the engine. */
    @Test
    fun `a connection failure is offline`() = runTest {
        val api = api { throw IOException("Connection refused") }

        // Not an identity check: coroutines' stack-trace recovery can hand back a copy.
        val cause = assertIs<DeskResult.Offline>(api.panel(PanelCommand.Toggle)).cause
        assertIs<IOException>(cause)
        assertEquals("Connection refused", cause.message)
    }

    @Test
    fun `two concurrent calls do not overlap`() = runTest {
        var inFlight = 0
        var maxInFlight = 0
        val api = api {
            inFlight++
            maxInFlight = maxOf(maxInFlight, inFlight)
            delay(100)
            inFlight--
            respond(OK_BODY)
        }

        val first = async { api.panel(PanelCommand.Toggle) }
        val second = async { api.screen(ScreenCommand.Next) }

        assertEquals(DeskResult.Success(Unit), first.await())
        assertEquals(DeskResult.Success(Unit), second.await())
        assertEquals(1, maxInFlight)
    }

    /** The engine runs on the test scheduler, so the handler's `delay` and the timeout use virtual time. */
    private fun TestScope.api(
        baseUrl: () -> String = { "http://desk.test" },
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): DeskApi {
        val config = MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler(handler)
        }
        return DeskApi(MockEngine(config), baseUrl)
    }

    private companion object {
        const val OK_BODY = """{"ok":true}"""
    }
}
