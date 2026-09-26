plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.serialization)
    alias(libs.plugins.deskmate.kmp.network)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.grappim.kit.coroutines)
        }
    }
}
