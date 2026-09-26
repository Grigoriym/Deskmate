package com.grappim.deskmate.widget

import com.grappim.deskmate.feature.display.domain.DisplayStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * What the widget shows, from the last successful `GET /api/status`. A `null` field means the
 * display had no data for it: [outdoorTempC] and [indoorTempC] when their section is `null`,
 * [nextDeparture] when `bvg` is `null` or no departure can be caught.
 */
@Serializable
data class WidgetSnapshot(
    val outdoorTempC: Int?,
    val indoorTempC: Double?,
    val nextDeparture: WidgetDeparture?,
    val fetchedAt: Instant
)

/** [time] is `"HH:MM"`, the display's local time. */
@Serializable
data class WidgetDeparture(val line: String, val direction: String, val time: String)

/** [fetchedAt] is when the status arrived; the widget shows it as "as of". */
fun DisplayStatus.toWidgetSnapshot(fetchedAt: Instant): WidgetSnapshot = WidgetSnapshot(
    outdoorTempC = outdoor?.tempC,
    indoorTempC = indoor?.tempC,
    nextDeparture = bvg?.departures?.firstOrNull()?.let {
        WidgetDeparture(line = it.line, direction = it.direction, time = it.time)
    },
    fetchedAt = fetchedAt
)
