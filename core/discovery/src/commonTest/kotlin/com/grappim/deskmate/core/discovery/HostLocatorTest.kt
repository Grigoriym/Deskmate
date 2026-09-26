package com.grappim.deskmate.core.discovery

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HostLocatorTest {

    private val store = FakeSavedHostStore()
    private val finder = FakeDisplayFinder()
    private val probe = FakeHostProbe()
    private val locator = HostLocator(store, finder, probe)

    @Test
    fun `saved host up is found without NSD`() = runTest {
        store.host = OLD_IP
        probe.up += OLD_IP

        locator.locate()

        assertEquals(HostState.Found(OLD_IP), locator.state.value)
        assertEquals(0, finder.calls)
    }

    @Test
    fun `saved host down and NSD finds a new IP saves the new IP`() = runTest {
        store.host = OLD_IP
        finder.result = NEW_IP
        probe.up += NEW_IP

        locator.locate()

        assertEquals(HostState.Found(NEW_IP), locator.state.value)
        assertEquals(NEW_IP, store.host)
    }

    @Test
    fun `NSD finds nothing so desk local is tried`() = runTest {
        probe.up += MDNS

        locator.locate()

        assertEquals(listOf(MDNS), probe.probed)
        assertEquals(HostState.Found(MDNS), locator.state.value)
        assertEquals(MDNS, store.host)
    }

    @Test
    fun `nothing passes is not found and the store keeps its host`() = runTest {
        store.host = OLD_IP
        finder.result = NEW_IP

        locator.locate()

        assertEquals(listOf(OLD_IP, NEW_IP, MDNS), probe.probed)
        assertEquals(HostState.NotFound, locator.state.value)
        assertEquals(OLD_IP, store.host)
    }

    @Test
    fun `rediscover skips the saved host`() = runTest {
        store.host = OLD_IP
        probe.up += OLD_IP
        finder.result = NEW_IP
        probe.up += NEW_IP

        locator.rediscover()

        assertFalse(OLD_IP in probe.probed)
        assertEquals(HostState.Found(NEW_IP), locator.state.value)
        assertEquals(NEW_IP, store.host)
    }

    @Test
    fun `manual IP that answers is saved and found`() = runTest {
        probe.up += NEW_IP

        assertTrue(locator.setManual(" 192.168.0.200 "))

        assertEquals(HostState.Found(NEW_IP), locator.state.value)
        assertEquals(NEW_IP, store.host)
    }

    @Test
    fun `manual IP that does not answer is not saved`() = runTest {
        locator.locate()

        assertFalse(locator.setManual("192.168.0.200"))

        assertNull(store.host)
        assertEquals(HostState.NotFound, locator.state.value)
    }

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

    private class FakeHostProbe : HostProbe {
        val up = mutableSetOf<String>()
        val probed = mutableListOf<String>()
        override suspend fun isDisplay(host: String): Boolean {
            probed += host
            return host in up
        }
    }

    private companion object {
        const val OLD_IP = "http://192.168.0.147"
        const val NEW_IP = "http://192.168.0.200"
        const val MDNS = "http://desk.local"
    }
}
