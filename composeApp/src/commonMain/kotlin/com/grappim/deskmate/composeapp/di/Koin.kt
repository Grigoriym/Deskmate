package com.grappim.deskmate.composeapp.di

import com.grappim.deskmate.core.api.ApiModule
import com.grappim.deskmate.core.api.DeskApi
import com.grappim.deskmate.core.discovery.DiscoveryModule
import com.grappim.deskmate.core.discovery.HostLocator
import com.grappim.deskmate.core.discovery.HostState
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * `composeApp` is Android-only, so `@ComponentScan` alone reaches every definition in this
 * compilation. Other Gradle modules' `@Module`s go into the explicit `includes`. [ApiModule] and
 * [DiscoveryModule] are `androidMain`-only; `commonMain` compiles against the Android variant.
 */
@Module(includes = [ApiModule::class, DiscoveryModule::class])
@Configuration
@ComponentScan("com.grappim.deskmate.composeapp")
class AppModule {

    /**
     * The display's address is the found host, read on every call. Call it only while the
     * locator state is [HostState.Found]; any other state throws `IllegalStateException`.
     */
    @Single
    fun provideDeskApi(engine: HttpClientEngine, locator: HostLocator): DeskApi = DeskApi(engine) {
        val state = locator.state.value
        check(state is HostState.Found) { "DeskApi called before the display was found: $state" }
        state.host
    }
}

@KoinApplication
object KoinApp
