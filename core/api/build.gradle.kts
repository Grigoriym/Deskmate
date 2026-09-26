plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.serialization)
    alias(libs.plugins.deskmate.kmp.network)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonTest {
            // The `status.example.json` fixture. Its own directory, so `feature:display:domain`
            // tests can compile the same file instead of a second copy.
            kotlin.srcDir("src/commonTestFixture/kotlin")
            dependencies {
                implementation(libs.ktor.client.mock)
            }
        }
    }
}
