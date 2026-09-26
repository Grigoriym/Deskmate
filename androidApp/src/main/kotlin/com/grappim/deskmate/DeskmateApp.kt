package com.grappim.deskmate

import android.app.Application
import com.grappim.deskmate.composeapp.di.KoinApp
import com.grappim.deskmate.widget.WidgetRefreshWorker
import com.grappim.kit.logger.TimberLogger
import org.koin.android.ext.koin.androidContext
import org.koin.plugin.module.dsl.startKoin
import timber.log.Timber

class DeskmateApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        TimberLogger.install()

        startKoin<KoinApp> {
            androidContext(this@DeskmateApp)
        }

        WidgetRefreshWorker.schedulePeriodic(this)
    }
}
