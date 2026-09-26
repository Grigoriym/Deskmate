package com.grappim.deskmate.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DeskmateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DeskmateWidget()
}
