package com.grappim.deskmate.feature.display.ui

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/** A module in another Gradle module: `composeApp`'s `AppModule` lists it in its `includes`. */
@Module
@Configuration
@ComponentScan("com.grappim.deskmate.feature.display.ui")
class DisplayUiModule
