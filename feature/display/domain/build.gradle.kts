plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.stability)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.api)
        }
        commonTest {
            // core:api's `status.example.json` fixture, compiled here too (one copy, three users).
            kotlin.srcDir("../../../core/api/src/commonTestFixture/kotlin")
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
