package com.grappim.deskmate.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /api/status`, field-for-field as in `../esp32-desk-display/docs/API.md`. Read that file
 * for what each field means; these classes only mirror its shape.
 *
 * Every section can be `null` on its own: `null` means "no data yet" (for `warning`, also "the
 * last fetch failed"). Values stay as the JSON sends them, strings included; the domain layer
 * types and formats them.
 */
@Serializable
data class StatusDto(
    val time: String,
    val date: String,
    val screen: String,
    @SerialName("panel_on") val panelOn: Boolean,
    val outdoor: OutdoorDto?,
    val indoor: IndoorDto?,
    val co2: Co2Dto?,
    val air: AirDto?,
    val warning: WarningDto?,
    @SerialName("next_holiday") val nextHoliday: NextHolidayDto?,
    val bvg: BvgDto?
)

@Serializable
data class OutdoorDto(
    @SerialName("temp_c") val tempC: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_kmh") val windKmh: Int,
    @SerialName("uv_max") val uvMax: Int,
    val sunrise: String,
    val sunset: String,
    val rain: RainDto
)

/** `from` and `until` are `""` (not absent) when there is no such hour. */
@Serializable
data class RainDto(@SerialName("in_h") val inH: Int, val from: String, val until: String)

@Serializable
data class IndoorDto(
    @SerialName("temp_c") val tempC: Double,
    @SerialName("humidity_pct") val humidityPct: Double,
    @SerialName("pressure_hpa") val pressureHpa: Double
)

@Serializable
data class Co2Dto(val ppm: Int)

@Serializable
data class AirDto(val aqi: Int, @SerialName("aqi_label") val aqiLabel: String, val pollen: PollenDto)

@Serializable
data class PollenDto(val alder: Int, val birch: Int, val grass: Int, val mugwort: Int, val ragweed: Int)

/** The fields after `count` are only present when `count` > 0, so they default to `null`. */
@Serializable
data class WarningDto(
    val count: Int,
    val event: String? = null,
    val severity: String? = null,
    val started: Boolean? = null,
    val onset: String? = null
)

@Serializable
data class NextHolidayDto(val date: String, val name: String)

@Serializable
data class BvgDto(
    @SerialName("walk_min") val walkMin: Int,
    @SerialName("walk_comfort") val walkComfort: Int,
    val departures: List<DepartureDto>
)

@Serializable
data class DepartureDto(val line: String, val direction: String, val time: String, @SerialName("in_min") val inMin: Int)
