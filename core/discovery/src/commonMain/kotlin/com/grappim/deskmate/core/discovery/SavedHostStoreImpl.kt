package com.grappim.deskmate.core.discovery

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [SavedHostStore::class])
internal class SavedHostStoreImpl(private val dataStore: DataStore<Preferences>) : SavedHostStore {

    override suspend fun read(): String? = dataStore.data.first()[KEY_HOST]

    override suspend fun save(host: String) {
        dataStore.edit { prefs -> prefs[KEY_HOST] = host }
    }

    override suspend fun clear() {
        dataStore.edit { prefs -> prefs.remove(KEY_HOST) }
    }

    private companion object {
        private val KEY_HOST = stringPreferencesKey("host")
    }
}
