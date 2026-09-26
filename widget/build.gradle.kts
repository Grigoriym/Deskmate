plugins {
    alias(libs.plugins.deskmate.kmp.library)
    // The Compose compiler for Glance's `@Composable`s, and `androidResources` on for the
    // `appwidget-provider` XML.
    alias(libs.plugins.deskmate.kmp.library.compose)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.glance.appwidget)
        }
    }
}
