plugins {
    alias(libs.plugins.deskmate.kmp.library)
    // The Compose compiler for Glance's `@Composable`s, and `androidResources` on for the
    // `appwidget-provider` XML.
    alias(libs.plugins.deskmate.kmp.library.compose)
    // The snapshot is stored as one JSON string.
    alias(libs.plugins.deskmate.kmp.serialization)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.glance.appwidget)
        }
        commonMain.dependencies {
            implementation(projects.feature.display.domain)
            implementation(libs.androidx.datastore.preferences.core)
        }
        commonTest {
            // core:api's `status.example.json` fixture, compiled here too (one copy, three users).
            kotlin.srcDir("../core/api/src/commonTestFixture/kotlin")
        }
    }
}
