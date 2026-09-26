package com.grappim.deskmate.widget

import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.api.DeskResult
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.feature.display.domain.toDisplayStatus
import com.grappim.kit.logger.logcat
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Single
import kotlin.time.Clock

/**
 * One background fetch for the widget: `GET /api/status`, mapped and saved as the new
 * [WidgetSnapshot]. Runs without the app on screen, so the locator can still be in
 * [HostState.Searching]: then it runs `locate()` first.
 *
 * Any failure keeps the last snapshot. No `rediscover()` here: the app's poll owns that.
 */
@Single
class WidgetRefresher(
    private val locator: HostLocator,
    private val api: DeskApi,
    private val store: WidgetSnapshotStore
) {
    suspend fun refresh() {
        if (locator.state.value !is HostState.Found) locator.locate()
        if (locator.state.value !is HostState.Found) {
            logcat { "Widget refresh: display not found, snapshot kept" }
            return
        }
        // DeskApi throws `IllegalStateException` outside `Found`. The app's poll can start a
        // `rediscover()` between the check above and the call.
        val result = try {
            api.status()
        } catch (e: IllegalStateException) {
            // `CancellationException` is an `IllegalStateException` too.
            if (e is CancellationException) throw e
            logcat { "Widget refresh skipped: ${e.message}" }
            return
        }
        if (result is DeskResult.Success) {
            store.save(result.value.toDisplayStatus().toWidgetSnapshot(Clock.System.now()))
            logcat { "Widget refresh: snapshot saved" }
        } else {
            logcat { "Widget refresh failed (${result::class.simpleName}), snapshot kept" }
        }
    }
}
