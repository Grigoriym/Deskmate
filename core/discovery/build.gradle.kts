plugins {
    alias(libs.plugins.deskmate.kmp.library)
    alias(libs.plugins.deskmate.kmp.di)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // `preferencesDataStoreFile`, for the file path under the app's files dir.
            implementation(libs.androidx.datastore.preferences)
        }
        commonMain.dependencies {
            implementation(libs.androidx.datastore.preferences.core)
        }
    }
}
