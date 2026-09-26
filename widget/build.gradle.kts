plugins {
    alias(libs.plugins.deskmate.kmp.library)
    // The Compose compiler for Glance's `@Composable`s, and `androidResources` on for the
    // `appwidget-provider` XML.
    alias(libs.plugins.deskmate.kmp.library.compose)
    // The snapshot is stored as one JSON string.
    alias(libs.plugins.deskmate.kmp.serialization)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.work.runtime)
            // `preferencesDataStoreFile`, for the file path under the app's files dir.
            implementation(libs.androidx.datastore.preferences)
        }
        commonMain.dependencies {
            implementation(projects.feature.display.domain)
            implementation(projects.core.discovery)
            implementation(libs.androidx.datastore.preferences.core)
        }
        commonTest {
            // core:api's `status.example.json` fixture, compiled here too (one copy, three users).
            kotlin.srcDir("../core/api/src/commonTestFixture/kotlin")
            dependencies {
                implementation(libs.ktor.client.mock)
            }
        }
    }
}
