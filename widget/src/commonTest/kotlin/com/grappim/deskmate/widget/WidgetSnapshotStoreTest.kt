package com.grappim.deskmate.widget

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
import kotlin.time.Instant

class WidgetSnapshotStoreTest {

    private val dir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "widget-snapshot-${Random.nextLong()}"
    private val file: Path = dir / "widget.preferences_pb"

    @AfterTest
    fun deleteDir() {
        FileSystem.SYSTEM.deleteRecursively(dir)
    }

    /** One active DataStore per file: see `SavedHostStoreImplTest`. */
    private fun newStore(scope: CoroutineScope) = WidgetSnapshotStore(
        PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { file })
    )

    @Test
    fun `an empty store reads null`() = runTest {
        assertNull(newStore(backgroundScope).read())
    }

    @Test
    fun `a saved snapshot reads back`() = runTest {
        val store = newStore(backgroundScope)

        store.save(FULL)

        assertEquals(FULL, store.read())
    }

    @Test
    fun `a snapshot with every field null reads back`() = runTest {
        val store = newStore(backgroundScope)
        val empty = FULL.copy(outdoorTempC = null, indoorTempC = null, nextDeparture = null)

        store.save(empty)

        assertEquals(empty, store.read())
    }

    @Test
    fun `a new snapshot replaces the old one`() = runTest {
        val store = newStore(backgroundScope)
        val newer = FULL.copy(outdoorTempC = 9, fetchedAt = Instant.parse("2026-09-26T16:00:00Z"))
        store.save(FULL)

        store.save(newer)

        assertEquals(newer, store.read())
    }

    @Test
    fun `a saved snapshot survives a new store on the same file`() = runTest {
        val firstJob = Job()
        newStore(CoroutineScope(coroutineContext + firstJob)).save(FULL)
        firstJob.cancelAndJoin()

        assertEquals(FULL, newStore(backgroundScope).read())
    }

    private companion object {
        val FETCHED_AT = Instant.parse("2026-09-26T15:42:10Z")
        val FULL = WidgetSnapshot(
            outdoorTempC = 14,
            indoorTempC = 23.1,
            nextDeparture = WidgetDeparture(line = "U5", direction = "HAUPTBAHNHOF", time = "17:55"),
            fetchedAt = FETCHED_AT
        )
    }
}
