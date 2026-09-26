package com.grappim.deskmate.composeapp.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

/**
 * `composeApp` is Android-only, so `@ComponentScan` alone reaches every definition in this
 * compilation. Other Gradle modules' `@Module`s (M1+) go into an explicit `includes = [...]`.
 */
@Module
@Configuration
@ComponentScan("com.grappim.deskmate.composeapp")
class AppModule

@KoinApplication
object KoinApp
