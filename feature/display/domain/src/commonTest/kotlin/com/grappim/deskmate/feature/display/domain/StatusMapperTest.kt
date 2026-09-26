package com.grappim.deskmate.feature.display.domain

import com.grappim.deskmate.core.api.dto.BvgDto
import com.grappim.deskmate.core.api.dto.Co2Dto
import com.grappim.deskmate.core.api.dto.DepartureDto
import com.grappim.deskmate.core.api.dto.RainDto
import com.grappim.deskmate.core.api.dto.StatusDto
import com.grappim.deskmate.core.api.dto.WarningDto
import com.grappim.deskmate.core.api.dto.statusExampleJson
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatusMapperTest {
    private val example: StatusDto = Json.decodeFromString(statusExampleJson)

    private fun outdoor() = requireNotNull(example.outdoor)
    private fun air() = requireNotNull(example.air)

    private fun mapRain(rain: RainDto): Rain =
        requireNotNull(example.copy(outdoor = outdoor().copy(rain = rain)).toDisplayStatus().outdoor).rain

    private fun mapWeather(code: Int): WeatherGroup =
        requireNotNull(example.copy(outdoor = outdoor().copy(weatherCode = code)).toDisplayStatus().outdoor).weather

    private fun mapCo2(ppm: Int): Co2Band = requireNotNull(example.copy(co2 = Co2Dto(ppm)).toDisplayStatus().co2).band

    private fun departure(inMin: Int) =
        DepartureDto(line = "U5", direction = "HAUPTBAHNHOF", time = "18:00", inMin = inMin)

    /** walk_min 6, walk_comfort 11, as in the example. */
    private fun mapBvg(vararg inMins: Int): Bvg = requireNotNull(
        example.copy(bvg = BvgDto(walkMin = 6, walkComfort = 11, departures = inMins.map(::departure)))
            .toDisplayStatus()
            .bvg
    )

    private fun mapPollen(grains: Int): PollenLevel {
        val pollen = air().pollen.copy(
            alder = grains,
            birch = grains,
            grass = grains,
            mugwort = grains,
            ragweed = grains
        )
        val mapped = requireNotNull(example.copy(air = air().copy(pollen = pollen)).toDisplayStatus().air).pollen
        val levels = setOf(mapped.alder, mapped.birch, mapped.grass, mapped.mugwort, mapped.ragweed)
        return levels.single()
    }

    @Test
    fun `the documented example maps to every field`() {
        val expected = DisplayStatus(
            clock = Clock.Synced(time = "17:42", date = "2026-09-26"),
            screen = Screen.HOME,
            panelOn = true,
            outdoor = Outdoor(
                tempC = 14,
                weather = WeatherGroup.RAIN,
                windKmh = 18,
                uvMax = 3,
                sunrise = "06:58",
                sunset = "18:55",
                rain = Rain.Until("20:00")
            ),
            indoor = Indoor(tempC = 23.1, humidityPct = 44.9, pressureHpa = 1014.2),
            co2 = Co2(ppm = 863, band = Co2Band.FINE),
            air = Air(
                aqi = 31,
                aqiLabel = AqiLabel.FAIR,
                pollen = Pollen(
                    alder = PollenLevel.NONE,
                    birch = PollenLevel.NONE,
                    grass = PollenLevel.MEDIUM,
                    mugwort = PollenLevel.LOW,
                    ragweed = PollenLevel.NONE
                )
            ),
            warning = Warning.Active(
                count = 1,
                event = "HEAVY RAIN",
                severity = Severity.MODERATE,
                started = false,
                onset = "19:00"
            ),
            nextHoliday = NextHoliday(name = "GERMAN UNITY DAY", date = "2026-10-03", isToday = false),
            // walk_min 6: the 17:45 train (in_min 3) is dropped. 13 - walk_comfort 11 = 2.
            bvg = Bvg(
                departures = listOf(Departure(line = "U5", direction = "HAUPTBAHNHOF", time = "17:55", inMin = 13)),
                hint = LeaveHint.LeaveIn(2)
            )
        )

        assertEquals(expected, example.toDisplayStatus())
    }

    // Clock and screen

    @Test
    fun `a 1970 date means the clock is not synced`() {
        val status = example.copy(time = "00:03", date = "1970-01-01").toDisplayStatus()

        assertEquals(Clock.NotSynced, status.clock)
    }

    @Test
    fun `each known screen maps to its enum value`() {
        val expected = mapOf(
            "home" to Screen.HOME,
            "outdoor" to Screen.OUTDOOR,
            "air" to Screen.AIR,
            "indoor" to Screen.INDOOR,
            "bvg" to Screen.BVG
        )

        expected.forEach { (json, screen) ->
            assertEquals(screen, example.copy(screen = json).toDisplayStatus().screen)
        }
    }

    @Test
    fun `an unknown screen maps to UNKNOWN`() {
        assertEquals(Screen.UNKNOWN, example.copy(screen = "clock").toDisplayStatus().screen)
    }

    // Weather

    @Test
    fun `weather codes map to the panel groups`() {
        val expected = mapOf(
            0 to WeatherGroup.SUN,
            1 to WeatherGroup.SUN,
            2 to WeatherGroup.CLOUD,
            3 to WeatherGroup.CLOUD,
            45 to WeatherGroup.CLOUD,
            48 to WeatherGroup.CLOUD,
            51 to WeatherGroup.RAIN,
            57 to WeatherGroup.RAIN,
            61 to WeatherGroup.RAIN,
            67 to WeatherGroup.RAIN,
            80 to WeatherGroup.RAIN,
            82 to WeatherGroup.RAIN,
            71 to WeatherGroup.SNOW,
            77 to WeatherGroup.SNOW,
            85 to WeatherGroup.SNOW,
            86 to WeatherGroup.SNOW,
            95 to WeatherGroup.STORM,
            99 to WeatherGroup.STORM
        )

        expected.forEach { (code, group) -> assertEquals(group, mapWeather(code), "code $code") }
    }

    @Test
    fun `codes outside the groups are cloud`() {
        listOf(-1, 4, 50, 58, 68, 79, 83, 84, 87, 94, 100).forEach {
            assertEquals(WeatherGroup.CLOUD, mapWeather(it), "code $it")
        }
    }

    // Rain

    @Test
    fun `in_h -1 is no rain`() {
        assertEquals(Rain.None, mapRain(RainDto(inH = -1, from = "", until = "")))
    }

    @Test
    fun `in_h above 0 is rain from`() {
        assertEquals(Rain.Starts("21:00"), mapRain(RainDto(inH = 3, from = "21:00", until = "23:00")))
    }

    @Test
    fun `in_h 0 with until is rain till`() {
        assertEquals(Rain.Until("20:00"), mapRain(RainDto(inH = 0, from = "17:00", until = "20:00")))
    }

    @Test
    fun `in_h 0 without until is rain for the whole window`() {
        assertEquals(Rain.AllWindow, mapRain(RainDto(inH = 0, from = "17:00", until = "")))
    }

    // CO2

    @Test
    fun `co2 band boundaries`() {
        val expected = mapOf(
            799 to Co2Band.FRESH,
            800 to Co2Band.FINE,
            999 to Co2Band.FINE,
            1000 to Co2Band.STUFFY,
            1400 to Co2Band.STUFFY,
            1401 to Co2Band.OPEN_WINDOW
        )

        expected.forEach { (ppm, band) -> assertEquals(band, mapCo2(ppm), "ppm $ppm") }
    }

    // Air

    @Test
    fun `each known aqi label maps to its enum value`() {
        val expected = mapOf(
            "GOOD" to AqiLabel.GOOD,
            "FAIR" to AqiLabel.FAIR,
            "MODERATE" to AqiLabel.MODERATE,
            "POOR" to AqiLabel.POOR,
            "VERY POOR" to AqiLabel.VERY_POOR,
            "EXTREME" to AqiLabel.EXTREME
        )

        expected.forEach { (json, label) ->
            val status = example.copy(air = air().copy(aqiLabel = json)).toDisplayStatus()
            assertEquals(label, status.air?.aqiLabel)
        }
    }

    @Test
    fun `an unknown aqi label maps to UNKNOWN`() {
        val status = example.copy(air = air().copy(aqiLabel = "HAZARDOUS")).toDisplayStatus()

        assertEquals(AqiLabel.UNKNOWN, status.air?.aqiLabel)
    }

    @Test
    fun `pollen level boundaries, for all five keys`() {
        val expected = mapOf(
            0 to PollenLevel.NONE,
            1 to PollenLevel.LOW,
            10 to PollenLevel.LOW,
            11 to PollenLevel.MEDIUM,
            50 to PollenLevel.MEDIUM,
            51 to PollenLevel.HIGH
        )

        expected.forEach { (grains, level) -> assertEquals(level, mapPollen(grains), "grains $grains") }
    }

    // Warning

    @Test
    fun `a null warning is unknown`() {
        assertEquals(Warning.Unknown, example.copy(warning = null).toDisplayStatus().warning)
    }

    @Test
    fun `a warning with count 0 is none`() {
        assertEquals(Warning.None, example.copy(warning = WarningDto(count = 0)).toDisplayStatus().warning)
    }

    @Test
    fun `each known severity maps to its enum value`() {
        val expected = mapOf(
            "minor" to Severity.MINOR,
            "moderate" to Severity.MODERATE,
            "severe" to Severity.SEVERE,
            "extreme" to Severity.EXTREME
        )

        expected.forEach { (json, severity) ->
            val warning = example.copy(warning = example.warning?.copy(severity = json)).toDisplayStatus().warning
            assertEquals(severity, (warning as Warning.Active).severity)
        }
    }

    @Test
    fun `an unknown severity maps to UNKNOWN`() {
        val warning = example.copy(warning = example.warning?.copy(severity = "purple")).toDisplayStatus().warning

        assertEquals(Severity.UNKNOWN, (warning as Warning.Active).severity)
    }

    @Test
    fun `a warning with count above 0 but a missing field is unknown`() {
        val warning = example.copy(warning = WarningDto(count = 2, event = "FROST")).toDisplayStatus().warning

        assertEquals(Warning.Unknown, warning)
    }

    // Next holiday

    @Test
    fun `a holiday on the display's date is today`() {
        val status = example.copy(date = "2026-10-03").toDisplayStatus()

        assertEquals(true, status.nextHoliday?.isToday)
    }

    // Null sections, each on its own

    @Test
    fun `a null outdoor maps to null and leaves the rest`() {
        val status = example.copy(outdoor = null).toDisplayStatus()

        assertNull(status.outdoor)
        assertEquals(example.toDisplayStatus().copy(outdoor = null), status)
    }

    @Test
    fun `a null indoor maps to null and leaves the rest`() {
        val status = example.copy(indoor = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(indoor = null), status)
    }

    @Test
    fun `a null co2 maps to null and leaves the rest`() {
        val status = example.copy(co2 = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(co2 = null), status)
    }

    @Test
    fun `a null air maps to null and leaves the rest`() {
        val status = example.copy(air = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(air = null), status)
    }

    @Test
    fun `a null warning leaves the rest`() {
        val status = example.copy(warning = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(warning = Warning.Unknown), status)
    }

    @Test
    fun `a null next_holiday maps to null and leaves the rest`() {
        val status = example.copy(nextHoliday = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(nextHoliday = null), status)
    }

    @Test
    fun `a null bvg maps to null and leaves the rest`() {
        val status = example.copy(bvg = null).toDisplayStatus()

        assertEquals(example.toDisplayStatus().copy(bvg = null), status)
    }

    // BVG, with walk_min 6 and walk_comfort 11

    @Test
    fun `leave in while in_min - walk_comfort is positive`() {
        assertEquals(LeaveHint.LeaveIn(1), mapBvg(12).hint)
    }

    @Test
    fun `go now when in_min - walk_comfort is 0`() {
        assertEquals(LeaveHint.GoNow, mapBvg(11).hint)
    }

    @Test
    fun `hurry when in_min - walk_comfort is below 0`() {
        assertEquals(LeaveHint.Hurry, mapBvg(10).hint)
    }

    @Test
    fun `in_min equal to walk_min is catchable`() {
        val bvg = mapBvg(6)

        assertEquals(listOf(6), bvg.departures.map { it.inMin })
        assertEquals(LeaveHint.Hurry, bvg.hint)
    }

    @Test
    fun `in_min below walk_min is dropped and the hint is for the next one`() {
        val bvg = mapBvg(5, 12, 20)

        assertEquals(listOf(12, 20), bvg.departures.map { it.inMin })
        assertEquals(LeaveHint.LeaveIn(1), bvg.hint)
    }

    @Test
    fun `an empty list has no departures and no hint`() {
        assertEquals(Bvg(departures = emptyList(), hint = null), mapBvg())
    }

    @Test
    fun `a list with no catchable departure has no departures and no hint`() {
        assertEquals(Bvg(departures = emptyList(), hint = null), mapBvg(0, 3, 5))
    }
}
