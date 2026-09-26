plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.compose)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
