package com.grappim.deskmate.widget

import com.grappim.deskmate.core.api.dto.BvgDto
import com.grappim.deskmate.core.api.dto.DepartureDto
import com.grappim.deskmate.core.api.dto.StatusDto
import com.grappim.deskmate.core.api.dto.statusExampleJson
import com.grappim.deskmate.feature.display.domain.toDisplayStatus
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class WidgetSnapshotTest {
    private val example: StatusDto = Json.decodeFromString(statusExampleJson)

    private fun map(dto: StatusDto) = dto.toDisplayStatus().toWidgetSnapshot(FETCHED_AT)

    @Test
    fun `the documented example maps to the expected snapshot`() {
        val expected = WidgetSnapshot(
            outdoorTempC = 14,
            indoorTempC = 23.1,
            // walk_min 6: the 17:45 train (in_min 3) can't be caught, so the next is 17:55.
            nextDeparture = WidgetDeparture(line = "U5", direction = "HAUPTBAHNHOF", time = "17:55"),
            fetchedAt = FETCHED_AT
        )

        assertEquals(expected, map(example))
    }

    @Test
    fun `a null outdoor gives a null outdoor temp and leaves the rest`() {
        assertEquals(map(example).copy(outdoorTempC = null), map(example.copy(outdoor = null)))
    }

    @Test
    fun `a null indoor gives a null indoor temp and leaves the rest`() {
        assertEquals(map(example).copy(indoorTempC = null), map(example.copy(indoor = null)))
    }

    @Test
    fun `a null bvg gives no departure and leaves the rest`() {
        assertEquals(map(example).copy(nextDeparture = null), map(example.copy(bvg = null)))
    }

    @Test
    fun `a bvg with no catchable departure gives no departure and leaves the rest`() {
        val departures = listOf(DepartureDto(line = "U5", direction = "HAUPTBAHNHOF", time = "17:45", inMin = 3))
        val status = example.copy(bvg = BvgDto(walkMin = 6, walkComfort = 11, departures = departures))

        assertEquals(map(example).copy(nextDeparture = null), map(status))
    }

    private companion object {
        val FETCHED_AT = Instant.parse("2026-09-26T15:42:10Z")
    }
}
