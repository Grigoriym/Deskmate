plugins {
    alias(libs.plugins.deskmate.android.application)
}

android {
    namespace = libs.versions.app.pkg.get()

    defaultConfig {
        applicationId = libs.versions.app.pkg.get()
        testApplicationId = "${libs.versions.app.pkg.get()}.test"

        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
    }
}

dependencies {
    implementation(project(":composeApp"))
    // `MainActivity` starts the search once the local network permission is answered.
    implementation(project(":core:discovery"))
    // The home-screen widget: its receiver comes in through `widget`'s manifest.
    implementation(project(":widget"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)

    // `TimberLogger` backs every `logcat { }`; Timber itself plants the logcat tree.
    implementation(libs.grappim.kit.logger)
    implementation(libs.timber)

    implementation(libs.androidx.activity.compose)
    implementation(libs.jetbrains.compose.ui.tooling.preview)
    debugImplementation(libs.jetbrains.compose.ui.tooling)
}
