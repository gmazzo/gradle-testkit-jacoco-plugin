package io.github.gmazzo.gradle.testkit.jacoco

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion
import org.jacoco.agent.rt.RT

class JacocoTestKitReportCoveragePlugin @Inject constructor(
    private val gradle: Gradle,
) : Plugin<Any> {

    override fun apply(target: Any) {
        if (GradleVersion.current() >= GradleVersion.version("8.1")) {
            gradle.dumpOnBuildFinished()

        } else {
            @Suppress("DEPRECATION")
            gradle.buildFinished { dumpCoverageData() }
        }
    }

    companion object {

        fun dumpCoverageData() = try {
            RT.getAgent()

        } catch (e: IllegalStateException) {
            // it may not be started of no instrumented classes are run
            e.printStackTrace()
            null
        }?.dump(true)

    }

}
