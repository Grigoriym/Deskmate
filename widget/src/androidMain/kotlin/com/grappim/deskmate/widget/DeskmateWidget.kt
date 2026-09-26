package com.grappim.deskmate.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

/** The home-screen widget. For now one static text; M4.4 renders the display's data. */
class DeskmateWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text("Deskmate")
        }
    }
}
