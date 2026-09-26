package com.grappim.deskmate.composeapp.greeting

import org.koin.core.annotation.Single

// M0.4 placeholder proving the Koin graph resolves an injection — M3 replaces the call site.
@Single
class GreetingProvider {
    @Suppress("FunctionOnlyReturningConstant")
    fun greeting(): String = "Deskmate (via Koin)"
}
