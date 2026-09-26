package com.grappim.deskmate.feature.display.domain

/**
 * `GET /api/status`, ready for the UI: every rule that decides something is already applied.
 * Values are typed, not display text; the UI picks the words. Field meanings are in
 * `../esp32-desk-display/docs/API.md`.
 *
 * A `null` section means "no data yet".
 */
data class DisplayStatus(
    val clock: Clock,
    val screen: Screen,
    val panelOn: Boolean,
    val outdoor: Outdoor?,
    val indoor: Indoor?,
    val co2: Co2?,
    val air: Air?,
    val warning: Warning,
    val nextHoliday: NextHoliday?,
    val bvg: Bvg?
)

sealed interface Clock {
    /** The display's `date` is in 1970: its clock has not synced since boot, so `time` is wrong too. */
    data object NotSynced : Clock

    /** `time` is `"HH:MM"`, `date` is `"YYYY-MM-DD"`, both the display's local time. */
    data class Synced(val time: String, val date: String) : Clock
}

/** The screen on the panel. [UNKNOWN] is a value this app does not know (a newer firmware). */
enum class Screen {
    HOME,
    OUTDOOR,
    AIR,
    INDOOR,
    BVG,
    UNKNOWN
}

data class Outdoor(
    val tempC: Int,
    val weather: WeatherGroup,
    val windKmh: Int,
    val uvMax: Int,
    val sunrise: String,
    val sunset: String,
    val rain: Rain
)

/** The panel's icon groups for a WMO weather code. */
enum class WeatherGroup {
    SUN,
    CLOUD,
    RAIN,
    SNOW,
    STORM
}

/** The panel's four rain cases, within the next 12 h. Times are `"HH:MM"`. */
sealed interface Rain {
    /** `NO RAIN 12H` */
    data object None : Rain

    /** `RAIN <from>`: not raining now, starts at [from]. */
    data class Starts(val from: String) : Rain

    /** `RAIN TILL <until>`: raining now, dry from [until]. */
    data class Until(val until: String) : Rain

    /** `RAIN NEXT 12H`: raining now, to the end of the window. */
    data object AllWindow : Rain
}

data class Indoor(val tempC: Double, val humidityPct: Double, val pressureHpa: Double)

data class Co2(val ppm: Int, val band: Co2Band)

/**
 * API.md's `co2` table. It gives the ranges as "below 800", "800-1000", "1000-1400" and "above
 * 1400", which share their end points. Reading used here: each band includes its lower bound,
 * and 1400 is the top of [STUFFY]. So 799 [FRESH], 800 [FINE], 1000 [STUFFY], 1400 [STUFFY],
 * 1401 [OPEN_WINDOW].
 */
enum class Co2Band {
    FRESH,
    FINE,
    STUFFY,
    OPEN_WINDOW
}

data class Air(val aqi: Int, val aqiLabel: AqiLabel, val pollen: Pollen)

/** API.md's `aqi_label` values. [UNKNOWN] is a label this app does not know. */
enum class AqiLabel {
    GOOD,
    FAIR,
    MODERATE,
    POOR,
    VERY_POOR,
    EXTREME,
    UNKNOWN
}

data class Pollen(
    val alder: PollenLevel,
    val birch: PollenLevel,
    val grass: PollenLevel,
    val mugwort: PollenLevel,
    val ragweed: PollenLevel
)

/** The panel's scale in grains/m³: < 1 none, 1-10 low, 11-50 medium, > 50 high. */
enum class PollenLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

sealed interface Warning {
    /** No successful fetch, or the last one failed. */
    data object Unknown : Warning

    /** Fetched, and no warning for Berlin. */
    data object None : Warning

    /**
     * The one warning the display picked. [count] is all warnings in effect or announced.
     * [onset] is `"HH:MM"` if today, else `"DD/MM"`.
     */
    data class Active(
        val count: Int,
        val event: String,
        val severity: Severity,
        val started: Boolean,
        val onset: String
    ) : Warning
}

/** DWD severity. [UNKNOWN] is a value this app does not know. */
enum class Severity {
    MINOR,
    MODERATE,
    SEVERE,
    EXTREME,
    UNKNOWN
}

/** [date] is `"YYYY-MM-DD"`. [isToday] when it equals the display's own date. */
data class NextHoliday(val name: String, val date: String, val isToday: Boolean)

/**
 * The departures that can still be caught on foot (`in_min` >= `walk_min`), soonest first. The
 * panel hides the others, so they are dropped here too. [hint] is for the first one; it is `null`
 * exactly when [departures] is empty (the panel shows `NO TRAINS` then).
 */
data class Bvg(val departures: List<Departure>, val hint: LeaveHint?)

/** [time] is `"HH:MM"`, local; [inMin] is minutes from the display's `time`. */
data class Departure(val line: String, val direction: String, val time: String, val inMin: Int)

/** The panel's hint for the first catchable departure: `in_min - walk_comfort`, by sign. */
sealed interface LeaveHint {
    /** `LEAVE IN <minutes>`, [minutes] > 0. */
    data class LeaveIn(val minutes: Int) : LeaveHint

    /** `GO NOW` */
    data object GoNow : LeaveHint

    /** `HURRY`: past the relaxed walk, but still catchable. */
    data object Hurry : LeaveHint
}
