plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.compose)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.grappim.kit.uikit)
            implementation(projects.feature.display.domain)
            implementation(projects.core.discovery)
            implementation(projects.strings)
        }
        commonTest {
            // core:api's `status.example.json` fixture, compiled here too (one copy, three users).
            kotlin.srcDir("../../../core/api/src/commonTestFixture/kotlin")
            dependencies {
                implementation(libs.ktor.client.mock)
            }
        }
    }
}
