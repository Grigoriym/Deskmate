package com.grappim.deskmate.widget

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.dto.StatusDto
import com.grappim.deskmate.core.api.dto.statusExampleJson
import com.grappim.deskmate.core.discovery.DisplayFinder
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostProbe
import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.core.discovery.SavedHostStore
import com.grappim.deskmate.feature.display.domain.toDisplayStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

private typealias Handler = suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData

/**
 * A real [HostLocator] over fakes. `locate()` reads the saved host; `rediscover()` skips it and
 * asks the finder. So [FakeSavedHostStore.reads] counts `locate()` calls, and a
 * [FakeDisplayFinder.calls] of 0 with the saved host up means no `rediscover()` ran.
 */
class WidgetRefresherTest {

    private val dir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "widget-refresher-${Random.nextLong()}"

    private val hostStore = FakeSavedHostStore()
    private val finder = FakeDisplayFinder()
    private val probe = FakeHostProbe()
    private val locator = HostLocator(hostStore, finder, probe)

    /** Each request as `"<method> <url>"`. */
    private val requests = mutableListOf<String>()
    private var handler: Handler = {
        respond(statusExampleJson)
    }

    @AfterTest
    fun deleteDir() {
        FileSystem.SYSTEM.deleteRecursively(dir)
    }

    @Test
    fun `searching locates once, then reads the status once and saves the snapshot`() = runTest {
        displayUp()
        val snapshots = newSnapshotStore(backgroundScope)
        val before = Clock.System.now()

        refresher(snapshots).refresh()

        assertEquals(1, hostStore.reads)
        assertEquals(listOf("GET $HOST/api/status"), requests)
        val saved = assertNotNull(snapshots.read())
        val expected = Json.decodeFromString<StatusDto>(statusExampleJson).toDisplayStatus()
            .toWidgetSnapshot(saved.fetchedAt)
        assertEquals(expected, saved)
        assertTrue(saved.fetchedAt >= before)
    }

    @Test
    fun `found already reads the status without a locate`() = runTest {
        displayUp()
        locator.locate()
        val snapshots = newSnapshotStore(backgroundScope)

        refresher(snapshots).refresh()

        assertEquals(1, hostStore.reads)
        assertEquals(1, requests.size)
        assertNotNull(snapshots.read())
    }

    @Test
    fun `offline keeps the old snapshot and does not rediscover`() = assertFailureKeepsSnapshot {
        throw IOException("Connection refused")
    }

    @Test
    fun `an http error keeps the old snapshot and does not rediscover`() = assertFailureKeepsSnapshot {
        respond("Status JSON failed", HttpStatusCode.InternalServerError)
    }

    @Test
    fun `an undecodable body keeps the old snapshot and does not rediscover`() = assertFailureKeepsSnapshot {
        respond("<html>captive portal</html>")
    }

    @Test
    fun `not found keeps the old snapshot and calls nothing`() = runTest {
        val snapshots = newSnapshotStore(backgroundScope)
        snapshots.save(OLD)

        refresher(snapshots).refresh()

        assertEquals(HostState.NotFound, locator.state.value)
        assertEquals(1, hostStore.reads)
        assertEquals(emptyList(), requests)
        assertEquals(OLD, snapshots.read())
    }

    private fun assertFailureKeepsSnapshot(failure: Handler) = runTest {
        displayUp()
        handler = failure
        val snapshots = newSnapshotStore(backgroundScope)
        snapshots.save(OLD)

        refresher(snapshots).refresh()

        assertEquals(1, requests.size)
        assertEquals(OLD, snapshots.read())
        assertEquals(1, hostStore.reads)
        assertEquals(0, finder.calls)
        assertEquals(HostState.Found(HOST), locator.state.value)
    }

    private fun displayUp() {
        hostStore.host = HOST
        probe.up += HOST
    }

    /** One active DataStore per file: see `SavedHostStoreImplTest`. */
    private fun newSnapshotStore(scope: CoroutineScope) = WidgetSnapshotStore(
        PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { dir / "widget.preferences_pb" })
    )

    private fun refresher(snapshots: WidgetSnapshotStore): WidgetRefresher {
        val engine = MockEngine { request ->
            requests += "${request.method.value} ${request.url}"
            handler(request)
        }
        val api = DeskApi(engine) { (locator.state.value as HostState.Found).host }
        return WidgetRefresher(locator, api, snapshots)
    }

    private class FakeSavedHostStore : SavedHostStore {
        var host: String? = null
        var reads = 0
        override suspend fun read(): String? {
            reads++
            return host
        }
        override suspend fun save(host: String) {
            this.host = host
        }
        override suspend fun clear() {
            host = null
        }
    }

    private class FakeDisplayFinder : DisplayFinder {
        var calls = 0
        override suspend fun find(): String? {
            calls++
            return null
        }
    }

    private class FakeHostProbe : HostProbe {
        val up = mutableSetOf<String>()
        override suspend fun isDisplay(host: String) = host in up
    }

    private companion object {
        const val HOST = "http://192.168.0.147"
        val OLD = WidgetSnapshot(
            outdoorTempC = 9,
            indoorTempC = 21.0,
            nextDeparture = null,
            fetchedAt = Instant.parse("2026-09-26T12:00:00Z")
        )
    }
}
