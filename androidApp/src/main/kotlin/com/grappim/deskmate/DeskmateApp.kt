package com.grappim.deskmate

import android.app.Application
import com.grappim.deskmate.composeapp.di.KoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.plugin.module.dsl.startKoin

class DeskmateApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin<KoinApp> {
            androidContext(this@DeskmateApp)
        }
    }
}
