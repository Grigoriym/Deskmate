plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.compose)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.grappim.kit.uikit)
        }
    }
}
