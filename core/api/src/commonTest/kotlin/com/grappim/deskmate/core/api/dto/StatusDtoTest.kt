package com.grappim.deskmate.core.api.dto

import com.grappim.deskmate.core.api.DeskJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusDtoTest {
    private val sections = listOf("outdoor", "indoor", "air", "warning", "next_holiday", "bvg")

    private val expected = StatusDto(
        time = "17:42",
        date = "2026-09-26",
        screen = "home",
        panelOn = true,
        outdoor = OutdoorDto(
            tempC = 14,
            weatherCode = 61,
            windKmh = 18,
            uvMax = 3,
            sunrise = "06:58",
            sunset = "18:55",
            rain = RainDto(inH = 0, from = "17:00", until = "20:00")
        ),
        indoor = IndoorDto(tempC = 23.1, humidityPct = 44.9, pressureHpa = 1014.2),
        air = AirDto(
            aqi = 31,
            aqiLabel = "FAIR",
            pollen = PollenDto(alder = 0, birch = 0, grass = 12, mugwort = 3, ragweed = 0)
        ),
        warning = WarningDto(
            count = 1,
            event = "HEAVY RAIN",
            severity = "moderate",
            started = false,
            onset = "19:00"
        ),
        nextHoliday = NextHolidayDto(date = "2026-10-03", name = "GERMAN UNITY DAY"),
        bvg = BvgDto(
            walkMin = 6,
            walkComfort = 11,
            departures = listOf(
                DepartureDto(line = "U5", direction = "HAUPTBAHNHOF", time = "17:45", inMin = 3),
                DepartureDto(line = "U5", direction = "HAUPTBAHNHOF", time = "17:55", inMin = 13)
            )
        )
    )

    private fun fixture(): JsonObject = DeskJson.parseToJsonElement(statusExampleJson).jsonObject

    private fun decode(json: JsonElement): StatusDto = DeskJson.decodeFromString(json.toString())

    @Test
    fun `the documented example decodes to every field`() {
        assertEquals(expected, DeskJson.decodeFromString<StatusDto>(statusExampleJson))
    }

    @Test
    fun `every section can be null at once`() {
        val json = JsonObject(fixture() + sections.associateWith { JsonNull })

        val expectedNulls = expected.copy(
            outdoor = null,
            indoor = null,
            air = null,
            warning = null,
            nextHoliday = null,
            bvg = null
        )
        assertEquals(expectedNulls, decode(json))
    }

    @Test
    fun `unknown fields are ignored at the top level and inside a section`() {
        val fixture = fixture()
        val outdoor = fixture.getValue("outdoor").jsonObject
        val json = JsonObject(
            fixture +
                ("firmware" to JsonPrimitive("1.2.3")) +
                ("outdoor" to JsonObject(outdoor + ("feels_like_c" to JsonPrimitive(12))))
        )

        assertEquals(expected, decode(json))
    }

    @Test
    fun `a warning with count 0 has no other fields`() {
        val json = JsonObject(fixture() + ("warning" to JsonObject(mapOf("count" to JsonPrimitive(0)))))

        assertEquals(WarningDto(count = 0), decode(json).warning)
    }
}
