package com.grappim.deskmate.feature.display.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.deskmate.core.api.PanelCommand
import com.grappim.deskmate.core.api.ScreenCommand
import com.grappim.deskmate.core.discovery.HostState
import com.grappim.deskmate.feature.display.domain.Air
import com.grappim.deskmate.feature.display.domain.AqiLabel
import com.grappim.deskmate.feature.display.domain.Bvg
import com.grappim.deskmate.feature.display.domain.Clock
import com.grappim.deskmate.feature.display.domain.Co2
import com.grappim.deskmate.feature.display.domain.Co2Band
import com.grappim.deskmate.feature.display.domain.DisplayStatus
import com.grappim.deskmate.feature.display.domain.Indoor
import com.grappim.deskmate.feature.display.domain.LeaveHint
import com.grappim.deskmate.feature.display.domain.NextHoliday
import com.grappim.deskmate.feature.display.domain.Outdoor
import com.grappim.deskmate.feature.display.domain.PollenLevel
import com.grappim.deskmate.feature.display.domain.Rain
import com.grappim.deskmate.feature.display.domain.Screen
import com.grappim.deskmate.feature.display.domain.Severity
import com.grappim.deskmate.feature.display.domain.Warning
import com.grappim.deskmate.feature.display.domain.WeatherGroup
import com.grappim.deskmate.strings.generated.resources.Res
import com.grappim.deskmate.strings.generated.resources.air_aqi
import com.grappim.deskmate.strings.generated.resources.air_title
import com.grappim.deskmate.strings.generated.resources.aqi_extreme
import com.grappim.deskmate.strings.generated.resources.aqi_fair
import com.grappim.deskmate.strings.generated.resources.aqi_good
import com.grappim.deskmate.strings.generated.resources.aqi_moderate
import com.grappim.deskmate.strings.generated.resources.aqi_poor
import com.grappim.deskmate.strings.generated.resources.aqi_unknown
import com.grappim.deskmate.strings.generated.resources.aqi_very_poor
import com.grappim.deskmate.strings.generated.resources.bvg_departure
import com.grappim.deskmate.strings.generated.resources.bvg_go_now
import com.grappim.deskmate.strings.generated.resources.bvg_hurry
import com.grappim.deskmate.strings.generated.resources.bvg_leave_in
import com.grappim.deskmate.strings.generated.resources.bvg_no_trains
import com.grappim.deskmate.strings.generated.resources.bvg_title
import com.grappim.deskmate.strings.generated.resources.clock_not_synced
import com.grappim.deskmate.strings.generated.resources.clock_title
import com.grappim.deskmate.strings.generated.resources.clock_value
import com.grappim.deskmate.strings.generated.resources.co2_fine
import com.grappim.deskmate.strings.generated.resources.co2_fresh
import com.grappim.deskmate.strings.generated.resources.co2_open_window
import com.grappim.deskmate.strings.generated.resources.co2_stuffy
import com.grappim.deskmate.strings.generated.resources.co2_title
import com.grappim.deskmate.strings.generated.resources.co2_value
import com.grappim.deskmate.strings.generated.resources.controls_current
import com.grappim.deskmate.strings.generated.resources.controls_title
import com.grappim.deskmate.strings.generated.resources.error_http
import com.grappim.deskmate.strings.generated.resources.error_undecodable
import com.grappim.deskmate.strings.generated.resources.holiday_title
import com.grappim.deskmate.strings.generated.resources.holiday_today
import com.grappim.deskmate.strings.generated.resources.holiday_value
import com.grappim.deskmate.strings.generated.resources.host_found
import com.grappim.deskmate.strings.generated.resources.host_searching
import com.grappim.deskmate.strings.generated.resources.indoor_humidity
import com.grappim.deskmate.strings.generated.resources.indoor_pressure
import com.grappim.deskmate.strings.generated.resources.indoor_temp
import com.grappim.deskmate.strings.generated.resources.indoor_title
import com.grappim.deskmate.strings.generated.resources.manual_ip_connect
import com.grappim.deskmate.strings.generated.resources.manual_ip_label
import com.grappim.deskmate.strings.generated.resources.manual_ip_placeholder
import com.grappim.deskmate.strings.generated.resources.manual_ip_rejected
import com.grappim.deskmate.strings.generated.resources.no_data
import com.grappim.deskmate.strings.generated.resources.not_found_body
import com.grappim.deskmate.strings.generated.resources.not_found_retry
import com.grappim.deskmate.strings.generated.resources.not_found_title
import com.grappim.deskmate.strings.generated.resources.outdoor_sun
import com.grappim.deskmate.strings.generated.resources.outdoor_temp
import com.grappim.deskmate.strings.generated.resources.outdoor_title
import com.grappim.deskmate.strings.generated.resources.outdoor_wind_uv
import com.grappim.deskmate.strings.generated.resources.panel_off
import com.grappim.deskmate.strings.generated.resources.panel_on
import com.grappim.deskmate.strings.generated.resources.panel_state_off
import com.grappim.deskmate.strings.generated.resources.panel_state_on
import com.grappim.deskmate.strings.generated.resources.panel_toggle
import com.grappim.deskmate.strings.generated.resources.pollen_alder
import com.grappim.deskmate.strings.generated.resources.pollen_birch
import com.grappim.deskmate.strings.generated.resources.pollen_grass
import com.grappim.deskmate.strings.generated.resources.pollen_high
import com.grappim.deskmate.strings.generated.resources.pollen_line
import com.grappim.deskmate.strings.generated.resources.pollen_low
import com.grappim.deskmate.strings.generated.resources.pollen_medium
import com.grappim.deskmate.strings.generated.resources.pollen_mugwort
import com.grappim.deskmate.strings.generated.resources.pollen_none
import com.grappim.deskmate.strings.generated.resources.pollen_ragweed
import com.grappim.deskmate.strings.generated.resources.rain_all_window
import com.grappim.deskmate.strings.generated.resources.rain_none
import com.grappim.deskmate.strings.generated.resources.rain_starts
import com.grappim.deskmate.strings.generated.resources.rain_until
import com.grappim.deskmate.strings.generated.resources.screen_air
import com.grappim.deskmate.strings.generated.resources.screen_bvg
import com.grappim.deskmate.strings.generated.resources.screen_home
import com.grappim.deskmate.strings.generated.resources.screen_indoor
import com.grappim.deskmate.strings.generated.resources.screen_next
import com.grappim.deskmate.strings.generated.resources.screen_outdoor
import com.grappim.deskmate.strings.generated.resources.screen_prev
import com.grappim.deskmate.strings.generated.resources.screen_unknown
import com.grappim.deskmate.strings.generated.resources.severity_extreme
import com.grappim.deskmate.strings.generated.resources.severity_minor
import com.grappim.deskmate.strings.generated.resources.severity_moderate
import com.grappim.deskmate.strings.generated.resources.severity_severe
import com.grappim.deskmate.strings.generated.resources.severity_unknown
import com.grappim.deskmate.strings.generated.resources.stale
import com.grappim.deskmate.strings.generated.resources.warning_count
import com.grappim.deskmate.strings.generated.resources.warning_none
import com.grappim.deskmate.strings.generated.resources.warning_started
import com.grappim.deskmate.strings.generated.resources.warning_title
import com.grappim.deskmate.strings.generated.resources.warning_unknown
import com.grappim.deskmate.strings.generated.resources.warning_upcoming
import com.grappim.deskmate.strings.generated.resources.weather_cloud
import com.grappim.deskmate.strings.generated.resources.weather_rain
import com.grappim.deskmate.strings.generated.resources.weather_snow
import com.grappim.deskmate.strings.generated.resources.weather_storm
import com.grappim.deskmate.strings.generated.resources.weather_sun
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Everything `/api/status` holds, one card per section, and the screen and panel commands.
 *
 * `collectAsStateWithLifecycle` is what stops the poll loop: [DisplayViewModel] polls only while
 * its state has a collector.
 */
@Composable
fun DisplayScreen(modifier: Modifier = Modifier, viewModel: DisplayViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DisplayContent(
        state = state,
        onRetry = viewModel::retry,
        onManualIp = viewModel::setManual,
        onScreen = viewModel::screen,
        onPanel = viewModel::panel,
        modifier = modifier
    )
}

@Composable
private fun DisplayContent(
    state: DisplayUiState,
    onRetry: () -> Unit,
    onManualIp: (String) -> Unit,
    onScreen: (ScreenCommand) -> Unit,
    onPanel: (PanelCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HostCard(state, onRetry, onManualIp) }
            if (state.isStale) item { NoticeCard(stringResource(Res.string.stale)) }
            state.error?.let { error -> item { NoticeCard(errorText(error)) } }
            state.status?.let { status ->
                item { ControlsCard(status, enabled = state.host is HostState.Found, onScreen, onPanel) }
                item { ClockCard(status.clock) }
                item { OutdoorCard(status.outdoor) }
                item { IndoorCard(status.indoor) }
                item { Co2Card(status.co2) }
                item { AirCard(status.air) }
                item { WarningCard(status.warning) }
                item { HolidayCard(status.nextHoliday) }
                item { BvgCard(status.bvg) }
            }
        }
    }
}

@Composable
private fun HostCard(state: DisplayUiState, onRetry: () -> Unit, onManualIp: (String) -> Unit) {
    when (val host = state.host) {
        HostState.Searching -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(stringResource(Res.string.host_searching), modifier = Modifier.padding(start = 12.dp))
        }

        is HostState.Found -> Text(
            text = stringResource(Res.string.host_found, host.host.removePrefix("http://")),
            style = MaterialTheme.typography.bodySmall
        )

        HostState.NotFound -> NotFoundCard(state.manualIpRejected, onRetry, onManualIp)
    }
}

@Composable
private fun NotFoundCard(manualIpRejected: Boolean, onRetry: () -> Unit, onManualIp: (String) -> Unit) {
    var ip by rememberSaveable { mutableStateOf("") }
    SectionCard(stringResource(Res.string.not_found_title)) {
        Text(stringResource(Res.string.not_found_body))
        Button(onClick = onRetry) { Text(stringResource(Res.string.not_found_retry)) }
        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.manual_ip_label)) },
            placeholder = { Text(stringResource(Res.string.manual_ip_placeholder)) },
            isError = manualIpRejected,
            supportingText = if (manualIpRejected) {
                { Text(stringResource(Res.string.manual_ip_rejected)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onManualIp(ip) })
        )
        OutlinedButton(onClick = { onManualIp(ip) }, enabled = ip.isNotBlank()) {
            Text(stringResource(Res.string.manual_ip_connect))
        }
    }
}

@Composable
private fun NoticeCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}

@Composable
private fun errorText(error: DisplayError): String = when (error) {
    is DisplayError.Http -> stringResource(Res.string.error_http, error.status, error.body)
    DisplayError.Undecodable -> stringResource(Res.string.error_undecodable)
}

@Composable
private fun ControlsCard(
    status: DisplayStatus,
    enabled: Boolean,
    onScreen: (ScreenCommand) -> Unit,
    onPanel: (PanelCommand) -> Unit
) {
    SectionCard(stringResource(Res.string.controls_title)) {
        val panel = stringResource(if (status.panelOn) Res.string.panel_state_on else Res.string.panel_state_off)
        Text(stringResource(Res.string.controls_current, stringResource(status.screen.label()), panel))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SCREEN_BUTTONS.forEach { (command, label) ->
                val current = command.shows() == status.screen
                if (current) {
                    Button(onClick = { onScreen(command) }, enabled = enabled) { Text(stringResource(label)) }
                } else {
                    OutlinedButton(onClick = { onScreen(command) }, enabled = enabled) { Text(stringResource(label)) }
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PANEL_BUTTONS.forEach { (command, label) ->
                OutlinedButton(onClick = { onPanel(command) }, enabled = enabled) { Text(stringResource(label)) }
            }
        }
    }
}

private val SCREEN_BUTTONS = listOf(
    ScreenCommand.Prev to Res.string.screen_prev,
    ScreenCommand.Next to Res.string.screen_next,
    ScreenCommand.Home to Res.string.screen_home,
    ScreenCommand.Outdoor to Res.string.screen_outdoor,
    ScreenCommand.Air to Res.string.screen_air,
    ScreenCommand.Indoor to Res.string.screen_indoor,
    ScreenCommand.Bvg to Res.string.screen_bvg
)

private val PANEL_BUTTONS = listOf(
    PanelCommand.On to Res.string.panel_on,
    PanelCommand.Off to Res.string.panel_off,
    PanelCommand.Toggle to Res.string.panel_toggle
)

/** The screen this command shows; `null` for next/prev. */
private fun ScreenCommand.shows(): Screen? = when (this) {
    ScreenCommand.Next, ScreenCommand.Prev -> null
    ScreenCommand.Home -> Screen.HOME
    ScreenCommand.Outdoor -> Screen.OUTDOOR
    ScreenCommand.Air -> Screen.AIR
    ScreenCommand.Indoor -> Screen.INDOOR
    ScreenCommand.Bvg -> Screen.BVG
}

private fun Screen.label(): StringResource = when (this) {
    Screen.HOME -> Res.string.screen_home
    Screen.OUTDOOR -> Res.string.screen_outdoor
    Screen.AIR -> Res.string.screen_air
    Screen.INDOOR -> Res.string.screen_indoor
    Screen.BVG -> Res.string.screen_bvg
    Screen.UNKNOWN -> Res.string.screen_unknown
}

@Composable
private fun ClockCard(clock: Clock) {
    SectionCard(stringResource(Res.string.clock_title)) {
        when (clock) {
            Clock.NotSynced -> Text(
                stringResource(Res.string.clock_not_synced),
                color = MaterialTheme.colorScheme.error
            )

            is Clock.Synced -> Text(stringResource(Res.string.clock_value, clock.time, clock.date))
        }
    }
}

@Composable
private fun OutdoorCard(outdoor: Outdoor?) {
    SectionCard(stringResource(Res.string.outdoor_title), outdoor) {
        Text(stringResource(Res.string.outdoor_temp, it.tempC, stringResource(it.weather.label())))
        Text(rainText(it.rain))
        Text(stringResource(Res.string.outdoor_wind_uv, it.windKmh, it.uvMax))
        Text(stringResource(Res.string.outdoor_sun, it.sunrise, it.sunset))
    }
}

private fun WeatherGroup.label(): StringResource = when (this) {
    WeatherGroup.SUN -> Res.string.weather_sun
    WeatherGroup.CLOUD -> Res.string.weather_cloud
    WeatherGroup.RAIN -> Res.string.weather_rain
    WeatherGroup.SNOW -> Res.string.weather_snow
    WeatherGroup.STORM -> Res.string.weather_storm
}

@Composable
private fun rainText(rain: Rain): String = when (rain) {
    Rain.None -> stringResource(Res.string.rain_none)
    is Rain.Starts -> stringResource(Res.string.rain_starts, rain.from)
    is Rain.Until -> stringResource(Res.string.rain_until, rain.until)
    Rain.AllWindow -> stringResource(Res.string.rain_all_window)
}

@Composable
private fun IndoorCard(indoor: Indoor?) {
    SectionCard(stringResource(Res.string.indoor_title), indoor) {
        Text(stringResource(Res.string.indoor_temp, it.tempC.toString()))
        Text(stringResource(Res.string.indoor_humidity, it.humidityPct.toString()))
        Text(stringResource(Res.string.indoor_pressure, it.pressureHpa.toString()))
    }
}

@Composable
private fun Co2Card(co2: Co2?) {
    SectionCard(stringResource(Res.string.co2_title), co2) {
        val band = when (it.band) {
            Co2Band.FRESH -> Res.string.co2_fresh
            Co2Band.FINE -> Res.string.co2_fine
            Co2Band.STUFFY -> Res.string.co2_stuffy
            Co2Band.OPEN_WINDOW -> Res.string.co2_open_window
        }
        Text(stringResource(Res.string.co2_value, it.ppm, stringResource(band)))
    }
}

@Composable
private fun AirCard(air: Air?) {
    SectionCard(stringResource(Res.string.air_title), air) {
        Text(stringResource(Res.string.air_aqi, it.aqi, stringResource(it.aqiLabel.label())))
        listOf(
            Res.string.pollen_alder to it.pollen.alder,
            Res.string.pollen_birch to it.pollen.birch,
            Res.string.pollen_grass to it.pollen.grass,
            Res.string.pollen_mugwort to it.pollen.mugwort,
            Res.string.pollen_ragweed to it.pollen.ragweed
        ).forEach { (name, level) ->
            Text(stringResource(Res.string.pollen_line, stringResource(name), stringResource(level.label())))
        }
    }
}

private fun AqiLabel.label(): StringResource = when (this) {
    AqiLabel.GOOD -> Res.string.aqi_good
    AqiLabel.FAIR -> Res.string.aqi_fair
    AqiLabel.MODERATE -> Res.string.aqi_moderate
    AqiLabel.POOR -> Res.string.aqi_poor
    AqiLabel.VERY_POOR -> Res.string.aqi_very_poor
    AqiLabel.EXTREME -> Res.string.aqi_extreme
    AqiLabel.UNKNOWN -> Res.string.aqi_unknown
}

private fun PollenLevel.label(): StringResource = when (this) {
    PollenLevel.NONE -> Res.string.pollen_none
    PollenLevel.LOW -> Res.string.pollen_low
    PollenLevel.MEDIUM -> Res.string.pollen_medium
    PollenLevel.HIGH -> Res.string.pollen_high
}

/** `warning` is never `null` here: the domain maps a `null` warning to [Warning.Unknown]. */
@Composable
private fun WarningCard(warning: Warning) {
    SectionCard(stringResource(Res.string.warning_title)) {
        when (warning) {
            Warning.Unknown -> Text(stringResource(Res.string.warning_unknown))

            Warning.None -> Text(stringResource(Res.string.warning_none))

            is Warning.Active -> {
                val format = if (warning.started) Res.string.warning_started else Res.string.warning_upcoming
                val severity = stringResource(warning.severity.label())
                Text(
                    text = stringResource(format, warning.event, severity, warning.onset),
                    color = MaterialTheme.colorScheme.error
                )
                Text(stringResource(Res.string.warning_count, warning.count))
            }
        }
    }
}

private fun Severity.label(): StringResource = when (this) {
    Severity.MINOR -> Res.string.severity_minor
    Severity.MODERATE -> Res.string.severity_moderate
    Severity.SEVERE -> Res.string.severity_severe
    Severity.EXTREME -> Res.string.severity_extreme
    Severity.UNKNOWN -> Res.string.severity_unknown
}

@Composable
private fun HolidayCard(holiday: NextHoliday?) {
    SectionCard(stringResource(Res.string.holiday_title), holiday) {
        Text(
            if (it.isToday) {
                stringResource(Res.string.holiday_today, it.name)
            } else {
                stringResource(Res.string.holiday_value, it.name, it.date)
            }
        )
    }
}

@Composable
private fun BvgCard(bvg: Bvg?) {
    SectionCard(stringResource(Res.string.bvg_title), bvg) {
        when (val hint = it.hint) {
            null -> Text(stringResource(Res.string.bvg_no_trains))
            else -> Text(hintText(hint), style = MaterialTheme.typography.titleMedium)
        }
        it.departures.forEach { departure ->
            Text(
                stringResource(
                    Res.string.bvg_departure,
                    departure.line,
                    departure.direction,
                    departure.time,
                    departure.inMin
                )
            )
        }
    }
}

@Composable
private fun hintText(hint: LeaveHint): String = when (hint) {
    is LeaveHint.LeaveIn -> stringResource(Res.string.bvg_leave_in, hint.minutes)
    LeaveHint.GoNow -> stringResource(Res.string.bvg_go_now)
    LeaveHint.Hurry -> stringResource(Res.string.bvg_hurry)
}

/** A section that can be `null` ("no data yet"). */
@Composable
private fun <T : Any> SectionCard(title: String, value: T?, content: @Composable ColumnScope.(T) -> Unit) {
    SectionCard(title) {
        if (value == null) {
            Text(stringResource(Res.string.no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            content(value)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
