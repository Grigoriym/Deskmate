package com.grappim.deskmate.core.discovery

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedHostStoreImplTest {

    private val dir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "saved-host-${Random.nextLong()}"
    private val file: Path = dir / "discovery.preferences_pb"

    @AfterTest
    fun deleteDir() {
        FileSystem.SYSTEM.deleteRecursively(dir)
    }

    /**
     * DataStore allows one active instance per file. Each store gets its own scope, so a test
     * can cancel it before it opens a second store on the same file.
     */
    private fun newStore(scope: CoroutineScope) = SavedHostStoreImpl(
        PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { file })
    )

    @Test
    fun `an empty store reads null`() = runTest {
        val store = newStore(backgroundScope)

        assertNull(store.read())
    }

    @Test
    fun `a saved host reads back`() = runTest {
        val store = newStore(backgroundScope)

        store.save(HOST)

        assertEquals(HOST, store.read())
    }

    @Test
    fun `a saved host survives a new store on the same file`() = runTest {
        val firstJob = Job()
        newStore(CoroutineScope(coroutineContext + firstJob)).save(HOST)
        firstJob.cancelAndJoin()

        val second = newStore(backgroundScope)

        assertEquals(HOST, second.read())
    }

    @Test
    fun `clear empties the store`() = runTest {
        val store = newStore(backgroundScope)
        store.save(HOST)

        store.clear()

        assertNull(store.read())
    }

    private companion object {
        const val HOST = "http://192.168.0.147"
    }
}
