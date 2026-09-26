plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.serialization)
    alias(libs.plugins.deskmate.kmp.network)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
