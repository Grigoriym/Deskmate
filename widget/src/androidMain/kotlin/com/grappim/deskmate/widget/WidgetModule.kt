package com.grappim.deskmate.widget

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import okio.Path.Companion.toPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private const val STORAGE_FILE_NAME = "widget"

/**
 * In `androidMain` because the DataStore file path needs a `Context`. Android is the only
 * target, so this `@ComponentScan` also picks up the `commonMain` classes of this module.
 */
@Module
@Configuration
@ComponentScan("com.grappim.deskmate.widget")
class WidgetModule {

    /**
     * The store builds its own DataStore. A second `DataStore<Preferences>` bean would clash with
     * `DiscoveryModule`'s unqualified one.
     */
    @Single
    fun provideSnapshotStore(context: Context): WidgetSnapshotStore = WidgetSnapshotStore(
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { context.preferencesDataStoreFile(STORAGE_FILE_NAME).absolutePath.toPath() }
        )
    )
}
