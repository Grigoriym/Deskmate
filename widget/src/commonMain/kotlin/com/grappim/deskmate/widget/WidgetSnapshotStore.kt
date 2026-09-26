package com.grappim.deskmate.widget

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The last [WidgetSnapshot], so the widget can render without a network call. Its own DataStore
 * file, not Glance state: Glance state is per widget instance and Android-only, and this store
 * is one snapshot for all instances, testable in `commonTest`.
 *
 * Only a successful fetch calls [save]. A failure calls nothing, so the old snapshot stays.
 */
class WidgetSnapshotStore(private val dataStore: DataStore<Preferences>) {

    /**
     * The last snapshot, and each new one as it is saved. `null` when nothing was saved yet. Also
     * `null` when the stored JSON no longer decodes (an app update changed [WidgetSnapshot]): the
     * next successful fetch writes a new one.
     */
    val snapshots: Flow<WidgetSnapshot?> = dataStore.data.map { prefs ->
        prefs[KEY_SNAPSHOT]?.let { json ->
            try {
                Json.decodeFromString<WidgetSnapshot>(json)
            } catch (_: SerializationException) {
                null
            }
        }
    }

    /** The current value of [snapshots]. */
    suspend fun read(): WidgetSnapshot? = snapshots.first()

    suspend fun save(snapshot: WidgetSnapshot) {
        dataStore.edit { prefs -> prefs[KEY_SNAPSHOT] = Json.encodeToString(snapshot) }
    }

    private companion object {
        private val KEY_SNAPSHOT = stringPreferencesKey("snapshot")
    }
}
