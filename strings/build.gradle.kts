plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.library.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `api`, not `implementation`: consumers need to resolve `StringResource` themselves.
            api(libs.jetbrains.compose.components.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "com.grappim.deskmate.strings.generated.resources"
    generateResClass = always
    publicResClass = true
}
