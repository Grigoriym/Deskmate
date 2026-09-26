package com.grappim.deskmate.widget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.grappim.deskmate.strings.generated.resources.Res
import com.grappim.deskmate.strings.generated.resources.bvg_no_trains
import com.grappim.deskmate.strings.generated.resources.no_data
import com.grappim.deskmate.strings.generated.resources.widget_as_of
import com.grappim.deskmate.strings.generated.resources.widget_departure
import com.grappim.deskmate.strings.generated.resources.widget_indoor
import com.grappim.deskmate.strings.generated.resources.widget_indoor_none
import com.grappim.deskmate.strings.generated.resources.widget_outdoor
import com.grappim.deskmate.strings.generated.resources.widget_outdoor_none
import com.grappim.deskmate.strings.generated.resources.widget_refresh
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

/**
 * The home-screen widget: the last [WidgetSnapshot], a refresh button, and a tap that opens the
 * app.
 *
 * Glance calls [provideGlance] once per session. An `update()` while the session runs only
 * recomposes, so the content collects [WidgetSnapshotStore.snapshots] instead of reading once.
 *
 * The text comes from the `strings` module through CMP `getString`, so all user text stays in one
 * place. `getString` is `suspend`, so the text is resolved in the flow, outside the composition.
 */
class DeskmateWidget :
    GlanceAppWidget(),
    KoinComponent {

    private val store: WidgetSnapshotStore by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val lines = store.snapshots.map { it?.toLines(context) }
        val initial = lines.first()
        val noData = getString(Res.string.no_data)
        val refresh = getString(Res.string.widget_refresh)
        val openApp = requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "The app has no launcher activity"
        }
        provideContent {
            val current by lines.collectAsState(initial)
            GlanceTheme {
                WidgetContent(
                    lines = current,
                    noData = noData,
                    refreshDescription = refresh,
                    onOpen = actionStartActivity(openApp),
                    onRefresh = actionRunCallback<RefreshAction>()
                )
            }
        }
    }
}

/** The widget's text, ready to show. */
private data class WidgetLines(val outdoor: String, val indoor: String, val departure: String, val asOf: String)

private suspend fun WidgetSnapshot.toLines(context: Context): WidgetLines {
    val outdoor = outdoorTempC?.let { getString(Res.string.widget_outdoor, it) }
        ?: getString(Res.string.widget_outdoor_none)
    val indoor = indoorTempC?.let { getString(Res.string.widget_indoor, it.toString()) }
        ?: getString(Res.string.widget_indoor_none)
    // The time before the direction: on a narrow widget the direction gets cut, not the time.
    val departure = nextDeparture?.let { getString(Res.string.widget_departure, it.line, it.time, it.direction) }
        ?: getString(Res.string.bvg_no_trains)
    // The phone's own time format (12 or 24 h), in its time zone.
    val time = DateFormat.getTimeFormat(context).format(Date(fetchedAt.toEpochMilliseconds()))
    return WidgetLines(
        outdoor = outdoor,
        indoor = indoor,
        departure = departure,
        asOf = getString(Res.string.widget_as_of, time)
    )
}

@Composable
private fun WidgetContent(
    lines: WidgetLines?,
    noData: String,
    refreshDescription: String,
    onOpen: Action,
    onRefresh: Action
) {
    val color = GlanceTheme.colors.onSurface
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onOpen),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lines == null) {
            Text(noData, style = TextStyle(color = color, fontSize = 14.sp))
        } else {
            val temp = TextStyle(color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row {
                Text(lines.outdoor, style = temp, maxLines = 1)
                Spacer(GlanceModifier.width(8.dp))
                Text(lines.indoor, style = temp, maxLines = 1)
            }
            Text(lines.departure, style = TextStyle(color = color, fontSize = 12.sp), maxLines = 1)
        }
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                lines?.asOf.orEmpty(),
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = color, fontSize = 11.sp),
                maxLines = 1
            )
            Image(
                provider = ImageProvider(R.drawable.ic_widget_refresh),
                contentDescription = refreshDescription,
                colorFilter = ColorFilter.tint(color),
                modifier = GlanceModifier.size(20.dp).clickable(onRefresh)
            )
        }
    }
}
