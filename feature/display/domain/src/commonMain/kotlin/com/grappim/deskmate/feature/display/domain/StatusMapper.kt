package com.grappim.deskmate.feature.display.domain

import com.grappim.deskmate.core.api.dto.AirDto
import com.grappim.deskmate.core.api.dto.BvgDto
import com.grappim.deskmate.core.api.dto.Co2Dto
import com.grappim.deskmate.core.api.dto.IndoorDto
import com.grappim.deskmate.core.api.dto.NextHolidayDto
import com.grappim.deskmate.core.api.dto.OutdoorDto
import com.grappim.deskmate.core.api.dto.PollenDto
import com.grappim.deskmate.core.api.dto.RainDto
import com.grappim.deskmate.core.api.dto.StatusDto
import com.grappim.deskmate.core.api.dto.WarningDto

private const val UNSYNCED_YEAR_PREFIX = "1970-"
private const val CO2_FINE_FROM = 800
private const val CO2_STUFFY_FROM = 1000
private const val CO2_STUFFY_TO = 1400
private const val POLLEN_LOW_TO = 10
private const val POLLEN_MEDIUM_TO = 50

fun StatusDto.toDisplayStatus(): DisplayStatus = DisplayStatus(
    clock = if (date.startsWith(UNSYNCED_YEAR_PREFIX)) Clock.NotSynced else Clock.Synced(time, date),
    screen = screen.toScreen(),
    panelOn = panelOn,
    outdoor = outdoor?.toOutdoor(),
    indoor = indoor?.toIndoor(),
    co2 = co2?.toCo2(),
    air = air?.toAir(),
    warning = warning.toWarning(),
    nextHoliday = nextHoliday?.toNextHoliday(today = date),
    bvg = bvg?.toBvg()
)

private fun String.toScreen(): Screen = when (this) {
    "home" -> Screen.HOME
    "outdoor" -> Screen.OUTDOOR
    "air" -> Screen.AIR
    "indoor" -> Screen.INDOOR
    "bvg" -> Screen.BVG
    else -> Screen.UNKNOWN
}

private fun OutdoorDto.toOutdoor() = Outdoor(
    tempC = tempC,
    weather = weatherGroup(weatherCode),
    windKmh = windKmh,
    uvMax = uvMax,
    sunrise = sunrise,
    sunset = sunset,
    rain = rain.toRain()
)

/** API.md's `weather_code` grouping; anything not listed is cloud. */
@Suppress("MagicNumber") // The WMO codes are the table itself.
private fun weatherGroup(code: Int): WeatherGroup = when (code) {
    0, 1 -> WeatherGroup.SUN
    in 51..57, in 61..67, in 80..82 -> WeatherGroup.RAIN
    in 71..77, 85, 86 -> WeatherGroup.SNOW
    in 95..99 -> WeatherGroup.STORM
    else -> WeatherGroup.CLOUD
}

/** Same order as the panel: `in_h` < 0, then > 0, then 0 with or without `until`. */
private fun RainDto.toRain(): Rain = when {
    inH < 0 -> Rain.None
    inH > 0 -> Rain.Starts(from)
    until.isNotEmpty() -> Rain.Until(until)
    else -> Rain.AllWindow
}

private fun IndoorDto.toIndoor() = Indoor(tempC = tempC, humidityPct = humidityPct, pressureHpa = pressureHpa)

private fun Co2Dto.toCo2() = Co2(ppm = ppm, band = co2Band(ppm))

/** The reading of API.md's ranges is in [Co2Band]. */
private fun co2Band(ppm: Int): Co2Band = when {
    ppm < CO2_FINE_FROM -> Co2Band.FRESH
    ppm < CO2_STUFFY_FROM -> Co2Band.FINE
    ppm <= CO2_STUFFY_TO -> Co2Band.STUFFY
    else -> Co2Band.OPEN_WINDOW
}

private fun AirDto.toAir() = Air(aqi = aqi, aqiLabel = aqiLabel.toAqiLabel(), pollen = pollen.toPollen())

private fun String.toAqiLabel(): AqiLabel = when (this) {
    "GOOD" -> AqiLabel.GOOD
    "FAIR" -> AqiLabel.FAIR
    "MODERATE" -> AqiLabel.MODERATE
    "POOR" -> AqiLabel.POOR
    "VERY POOR" -> AqiLabel.VERY_POOR
    "EXTREME" -> AqiLabel.EXTREME
    else -> AqiLabel.UNKNOWN
}

private fun PollenDto.toPollen() = Pollen(
    alder = pollenLevel(alder),
    birch = pollenLevel(birch),
    grass = pollenLevel(grass),
    mugwort = pollenLevel(mugwort),
    ragweed = pollenLevel(ragweed)
)

private fun pollenLevel(grains: Int): PollenLevel = when {
    grains < 1 -> PollenLevel.NONE
    grains <= POLLEN_LOW_TO -> PollenLevel.LOW
    grains <= POLLEN_MEDIUM_TO -> PollenLevel.MEDIUM
    else -> PollenLevel.HIGH
}

/**
 * API.md: the fields after `count` are present only when `count` > 0. If one is missing anyway,
 * the warning is [Warning.Unknown]: showing half a warning is worse than none.
 */
private fun WarningDto?.toWarning(): Warning {
    if (this == null) return Warning.Unknown
    if (count == 0) return Warning.None
    return Warning.Active(
        count = count,
        event = event ?: return Warning.Unknown,
        severity = severity?.toSeverity() ?: return Warning.Unknown,
        started = started ?: return Warning.Unknown,
        onset = onset ?: return Warning.Unknown
    )
}

private fun String.toSeverity(): Severity = when (this) {
    "minor" -> Severity.MINOR
    "moderate" -> Severity.MODERATE
    "severe" -> Severity.SEVERE
    "extreme" -> Severity.EXTREME
    else -> Severity.UNKNOWN
}

private fun NextHolidayDto.toNextHoliday(today: String) = NextHoliday(name = name, date = date, isToday = date == today)

/** Same as the panel: drop what can't be caught, then the hint for the first that can. */
private fun BvgDto.toBvg(): Bvg {
    val catchable = departures
        .filter { it.inMin >= walkMin }
        .map { Departure(line = it.line, direction = it.direction, time = it.time, inMin = it.inMin) }
    return Bvg(departures = catchable, hint = catchable.firstOrNull()?.let { leaveHint(it.inMin - walkComfort) })
}

private fun leaveHint(leaveIn: Int): LeaveHint = when {
    leaveIn > 0 -> LeaveHint.LeaveIn(leaveIn)
    leaveIn == 0 -> LeaveHint.GoNow
    else -> LeaveHint.Hurry
}
