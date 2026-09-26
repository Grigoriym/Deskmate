plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.compose)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.discovery)
            implementation(projects.feature.display.ui)
            // `WidgetModule` joins the app graph: the widget's worker resolves through it.
            implementation(projects.widget)
            // `DeskHostProbe` builds a `DeskApi` on the shared `HttpClientEngine`.
            implementation(libs.ktor.core)
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
