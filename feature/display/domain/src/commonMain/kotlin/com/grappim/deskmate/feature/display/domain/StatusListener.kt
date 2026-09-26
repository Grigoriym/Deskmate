package com.grappim.deskmate.feature.display.domain

/**
 * Gets each status that the app's poll fetched. The widget implements it: it saves the snapshot
 * and re-renders.
 *
 * Here for the module boundary: `feature:display:ui` is `commonMain` and Glance is Android-only,
 * and both modules already depend on this one.
 */
interface StatusListener {
    suspend fun onStatus(status: DisplayStatus)
}
