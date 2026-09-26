package com.grappim.deskmate.feature.display.ui

import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.ScreenCommand
import com.grappim.deskmate.core.api.dto.statusExampleJson
import com.grappim.deskmate.core.discovery.DisplayFinder
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostProbe
import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.core.discovery.SavedHostStore
import com.grappim.deskmate.feature.display.domain.DisplayStatus
import com.grappim.deskmate.feature.display.domain.StatusListener
import com.grappim.kit.testing.MainDispatcherRule
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DisplayViewModelTest {

    // `runTest` takes its scheduler from this Main dispatcher, so `viewModelScope`, the engine and
    // the test all run on one virtual clock.
    private val main = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeSavedHostStore()
    private val finder = FakeDisplayFinder()
    private val probe = FakeHostProbe()
    private val locator = HostLocator(store, finder, probe)
    private val listener = FakeStatusListener()

    /** Each request as `"<virtual ms> <method> <url>"`. */
    private val requests = mutableListOf<String>()
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respond(statusExampleJson)
    }

    @BeforeTest
    fun setUp() = main.setup()

    @AfterTest
    fun tearDown() = main.tearDown()

    @Test
    fun `polls every 5 s while found`() = runTest {
        val vm = foundViewModel()
        collect(vm)

        advanceTimeBy(10_001)

        assertEquals(listOf(0L, 5_000L, 10_000L), requests.map { it.substringBefore(' ').toLong() })
        assertNotNull(vm.uiState.value.status)
    }

    @Test
    fun `a successful poll saves one snapshot for the widget`() = runTest {
        val vm = foundViewModel()
        collect(vm)

        runCurrent()

        assertEquals(1, requests.size)
        assertEquals(listOf(vm.uiState.value.status), listener.statuses)
    }

    @Test
    fun `a failed poll saves no snapshot`() = runTest {
        val vm = foundViewModel()
        handler = { respond("Status JSON failed", HttpStatusCode.InternalServerError) }
        collect(vm)

        runCurrent()

        assertEquals(1, requests.size)
        assertEquals(emptyList(), listener.statuses)
    }

    @Test
    fun `no call while searching or not found`() = runTest {
        val vm = viewModel()
        collect(vm)
        advanceTimeBy(10_000)

        locator.locate()
        advanceTimeBy(10_000)

        assertEquals(HostState.NotFound, vm.uiState.value.host)
        assertEquals(emptyList(), requests)
    }

    @Test
    fun `offline keeps the data, marks it stale and rediscovers once`() = runTest {
        val vm = foundViewModel()
        collect(vm)
        runCurrent()
        val before = vm.uiState.value.status
        handler = { throw IOException("Connection refused") }
        probe.up.clear()

        advanceTimeBy(60_000)

        val state = vm.uiState.value
        assertTrue(state.isStale)
        assertEquals(before, state.status)
        assertEquals(HostState.NotFound, state.host)
        assertEquals(1, finder.calls)
        assertEquals(2, requests.size)
    }

    @Test
    fun `found again polls again and the next success clears stale`() = runTest {
        val vm = foundViewModel()
        val states = mutableListOf<DisplayUiState>()
        backgroundScope.launch { vm.uiState.collect { states += it } }
        runCurrent()
        // The old address is gone; the rediscover finds the display at a new DHCP address.
        handler = {
            if (it.url.toString().startsWith(HOST)) throw IOException("Connection refused")
            respond(statusExampleJson)
        }
        probe.up.clear()
        finder.result = NEW_HOST
        probe.up += NEW_HOST

        advanceTimeBy(5_001)

        assertTrue(states.any { it.isStale })
        val state = vm.uiState.value
        assertEquals(HostState.Found(NEW_HOST), state.host)
        assertFalse(state.isStale)
        assertEquals("5000 GET $NEW_HOST/api/status", requests.last())
        assertEquals(1, finder.calls)
    }

    @Test
    fun `an undecodable body shows an error and the loop keeps polling`() = runTest {
        val vm = foundViewModel()
        handler = { respond("<html>captive portal</html>") }
        collect(vm)

        advanceTimeBy(10_001)

        assertEquals(3, requests.size)
        assertEquals(DisplayError.Undecodable, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isStale)
    }

    @Test
    fun `an http error keeps the data and does not rediscover`() = runTest {
        val vm = foundViewModel()
        collect(vm)
        runCurrent()
        val before = vm.uiState.value.status
        handler = { respond("Status JSON failed", HttpStatusCode.InternalServerError) }

        advanceTimeBy(5_001)

        val state = vm.uiState.value
        assertEquals(DisplayError.Http(500, "Status JSON failed"), state.error)
        assertEquals(before, state.status)
        assertEquals(HostState.Found(HOST), state.host)
        assertEquals(0, finder.calls)
    }

    @Test
    fun `a command re-reads the status 150 ms later, then the normal poll goes on`() = runTest {
        val vm = foundViewModel()
        collect(vm)
        advanceTimeBy(1_000)

        vm.screen(ScreenCommand.Air)
        advanceTimeBy(5_001)

        assertEquals(
            listOf(
                "0 GET $HOST/api/status",
                "1000 POST $HOST/api/screen?go=air",
                "1150 GET $HOST/api/status",
                "5000 GET $HOST/api/status"
            ),
            requests
        )
    }

    @Test
    fun `polling stops when nothing collects`() = runTest {
        val vm = foundViewModel()
        val screen = collect(vm)
        advanceTimeBy(5_001)
        assertEquals(2, requests.size)

        screen.cancel()
        advanceTimeBy(60_000)

        assertEquals(2, requests.size)
    }

    @Test
    fun `a rejected manual IP is visible, an accepted one is found`() = runTest {
        val vm = viewModel()
        collect(vm)
        locator.locate()

        vm.setManual("192.168.0.200")
        runCurrent()
        assertTrue(vm.uiState.value.manualIpRejected)
        assertEquals(HostState.NotFound, vm.uiState.value.host)

        probe.up += NEW_HOST
        vm.setManual("192.168.0.200")
        runCurrent()
        assertFalse(vm.uiState.value.manualIpRejected)
        assertEquals(HostState.Found(NEW_HOST), vm.uiState.value.host)
    }

    @Test
    fun `retry tries the saved host again`() = runTest {
        // A saved manual IP: NSD and `desk.local` find nothing.
        store.host = HOST
        val vm = viewModel()
        collect(vm)
        locator.locate()
        runCurrent()
        assertEquals(HostState.NotFound, vm.uiState.value.host)

        probe.up += HOST
        vm.retry()
        runCurrent()

        assertEquals(HostState.Found(HOST), vm.uiState.value.host)
    }

    private suspend fun TestScope.foundViewModel(): DisplayViewModel {
        store.host = HOST
        probe.up += HOST
        locator.locate()
        return viewModel()
    }

    /** The engine runs on the test scheduler, like `DeskApiTest`'s. */
    private fun TestScope.viewModel(): DisplayViewModel {
        val config = MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler {
                requests += "${testScheduler.currentTime} ${it.method.value} ${it.url}"
                handler(it)
            }
        }
        val api = DeskApi(MockEngine(config)) { (locator.state.value as HostState.Found).host }
        return DisplayViewModel(locator, api, listener)
    }

    /** What the screen does while it is visible. */
    private fun TestScope.collect(vm: DisplayViewModel): Job = backgroundScope.launch { vm.uiState.collect {} }

    private class FakeSavedHostStore : SavedHostStore {
        var host: String? = null
        override suspend fun read() = host
        override suspend fun save(host: String) {
            this.host = host
        }
        override suspend fun clear() {
            host = null
        }
    }

    private class FakeDisplayFinder : DisplayFinder {
        var result: String? = null
        var calls = 0
        override suspend fun find(): String? {
            calls++
            return result
        }
    }

    private class FakeStatusListener : StatusListener {
        val statuses = mutableListOf<DisplayStatus>()
        override suspend fun onStatus(status: DisplayStatus) {
            statuses += status
        }
    }

    private class FakeHostProbe : HostProbe {
        val up = mutableSetOf<String>()
        override suspend fun isDisplay(host: String) = host in up
    }

    private companion object {
        const val HOST = "http://192.168.0.147"
        const val NEW_HOST = "http://192.168.0.200"
    }
}
