import com.grappim.deskmate.buildlogic.configureComposeStabilityConfig
import com.grappim.deskmate.buildlogic.configureComposeStabilityMarker
import com.grappim.deskmate.buildlogic.configureComposeStabilityReports
import org.gradle.api.Plugin
import org.gradle.api.Project

// Applied alongside `deskmate.kmp.library` on `*/domain` modules whose types are consumed as
// Composable parameters elsewhere — see wallosmobile's docs/compose/stability-reports.md.
class KmpLibraryStabilityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureComposeStabilityMarker()
            configureComposeStabilityReports()
            configureComposeStabilityConfig()
        }
    }
}
