package com.grappim.deskmate.composeapp.di

import android.content.Context
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * A missing Koin definition is otherwise a launch-time crash that no gate catches: a
 * `@ComponentScan` that misses a class, or an `AppModule` that forgets an `includes` line, both
 * compile.
 *
 * `verify()` checks every definition's constructor by reflection and fails on a parameter type
 * the module set can't supply, without instantiating anything.
 *
 * Limit: it does not check a provider function's own parameters. `provideDeskApi` takes a
 * `HostLocator`, but this test still passes with `DiscoveryModule` removed from `includes`
 * (checked in M2.4). Only a launch of the app catches that case.
 */
class KoinGraphTest {
    @Test
    fun `every definition in the app graph can be resolved`() {
        AppModule().module().verify(extraTypes = EXTERNALLY_SUPPLIED)
    }

    private companion object {
        /**
         * Types nothing in `AppModule` defines, on purpose:
         * - `Context` comes from `androidContext()` in `DeskmateApp`.
         * - `Function0` is a false positive. `verify()` reads a provider function's definition
         *   through the bound type's constructor, so for `provideDeskApi` it inspects
         *   `DeskApi(engine, baseUrl: () -> String)`. The provider builds that lambda itself;
         *   Koin is never asked for one.
         */
        val EXTERNALLY_SUPPLIED = listOf(Context::class, Function0::class)
    }
}
