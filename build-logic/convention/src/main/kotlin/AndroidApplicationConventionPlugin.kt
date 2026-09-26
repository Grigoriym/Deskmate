import com.android.build.api.dsl.ApplicationExtension
import com.grappim.deskmate.buildlogic.AppBuildTypes
import com.grappim.deskmate.buildlogic.configureComposeStabilityConfig
import com.grappim.deskmate.buildlogic.configureComposeStabilityReports
import com.grappim.deskmate.buildlogic.configureKotlinAndroid
import com.grappim.deskmate.buildlogic.configureLinting
import com.grappim.deskmate.buildlogic.configureTests
import com.grappim.deskmate.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            apply(plugin = "io.insert-koin.compiler.plugin")

            configureComposeStabilityReports()
            configureComposeStabilityConfig()

            extensions.configure<ApplicationExtension> {
                defaultConfig.apply {
                    targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                configureAppBuildTypes()

                bundle {
                    language {
                        enableSplit = false
                    }
                }

                packaging.resources.excludes.apply {
                    add("META-INF/ASL2.0")
                    add("META-INF/notice.txt")
                    add("META-INF/NOTICE.txt")
                    add("META-INF/NOTICE")
                    add("META-INF/license.txt")
                    add("DEPENDENCIES")
                }

                buildFeatures.apply {
                    compose = true
                }

                configureKotlinAndroid(this)
            }

            // `:androidApp` holds MainActivity and the Koin startup glue — real Kotlin that
            // the gates have to cover, even though it is not a KMP module.
            configureTests()
            configureLinting()
        }
    }
}

private fun ApplicationExtension.configureAppBuildTypes() {
    buildTypes {
        debug {
            applicationIdSuffix = AppBuildTypes.DEBUG.applicationIdSuffix

            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            applicationIdSuffix = AppBuildTypes.RELEASE.applicationIdSuffix

            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
