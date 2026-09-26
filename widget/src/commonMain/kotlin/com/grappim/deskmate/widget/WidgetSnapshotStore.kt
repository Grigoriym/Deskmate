package com.grappim.deskmate.widget

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
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
     * `null` when nothing was saved yet. Also `null` when the stored JSON no longer decodes (an
     * app update changed [WidgetSnapshot]): the next successful fetch writes a new one.
     */
    suspend fun read(): WidgetSnapshot? {
        val json = dataStore.data.first()[KEY_SNAPSHOT] ?: return null
        return try {
            Json.decodeFromString<WidgetSnapshot>(json)
        } catch (_: SerializationException) {
            null
        }
    }

    suspend fun save(snapshot: WidgetSnapshot) {
        dataStore.edit { prefs -> prefs[KEY_SNAPSHOT] = Json.encodeToString(snapshot) }
    }

    private companion object {
        private val KEY_SNAPSHOT = stringPreferencesKey("snapshot")
    }
}
