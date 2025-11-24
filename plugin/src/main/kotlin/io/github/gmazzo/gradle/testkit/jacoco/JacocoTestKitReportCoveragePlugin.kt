package io.github.gmazzo.gradle.testkit.jacoco

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion
import org.jacoco.agent.rt.RT

public class JacocoTestKitReportCoveragePlugin @Inject constructor(
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

    public companion object {

        public fun dumpCoverageData() {
            try {
                RT.getAgent().dump(true)

            } catch (e: IllegalStateException) {
                // it may not be started of no instrumented classes are run
                if (e.message?.contains("JaCoCo agent not started") != true) {
                    e.printStackTrace()
                }
            }
        }

    }

}
