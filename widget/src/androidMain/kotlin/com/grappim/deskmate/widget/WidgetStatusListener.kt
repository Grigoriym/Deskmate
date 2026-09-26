package com.grappim.deskmate.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.grappim.deskmate.feature.display.domain.DisplayStatus
import com.grappim.deskmate.feature.display.domain.StatusListener
import org.koin.core.annotation.Single
import kotlin.time.Clock

/**
 * The app's poll refreshes the widget: each status becomes the new snapshot, then every widget
 * instance re-renders.
 */
@Single(binds = [StatusListener::class])
class WidgetStatusListener(private val context: Context, private val store: WidgetSnapshotStore) : StatusListener {
    override suspend fun onStatus(status: DisplayStatus) {
        store.save(status.toWidgetSnapshot(Clock.System.now()))
        DeskmateWidget().updateAll(context)
    }
}
