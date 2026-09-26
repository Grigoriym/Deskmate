package com.grappim.deskmate.core.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** In `androidMain` because the OkHttp engine is Android-only. */
@Module
@Configuration
class ApiModule {

    /** One engine for every [DeskApi]: the app's own and the one that probes candidate hosts. */
    @Single
    fun provideEngine(): HttpClientEngine = OkHttp.create()
}
