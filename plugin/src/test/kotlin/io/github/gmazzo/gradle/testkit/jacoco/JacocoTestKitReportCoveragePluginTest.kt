@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.gmazzo.gradle.testkit.jacoco

import io.github.gmazzo.gradle.testkit.jacoco.JacocoTestKitReportCoveragePlugin.Companion.dumpCoverageData
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.internal.GradleInternal
import org.gradle.util.GradleVersion
import org.jacoco.agent.rt.IAgent
import org.jacoco.agent.rt.RT
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class JacocoTestKitReportCoveragePluginTest {

    val gradle: GradleInternal = mockk {
        every { buildFinished(any<Action<BuildResult>>()) } answers {
            firstArg<Action<BuildResult>>().execute(mockk())
        }
    }

    val agent: IAgent = mockk(relaxUnitFun = true)

    val plugin = JacocoTestKitReportCoveragePlugin(gradle)

    @ParameterizedTest(name = "gradle={0}, flowAPI={1}")
    @CsvSource(
        "7.0, false",
        "8.0, false",
        "8.1, true",
        "9.0, true",
    )
    fun `should report coverage when build finishes`(gradleVersion: String, flowAPI: Boolean) =
        mockkStatic(GradleVersion::current, GradleInternal::dumpOnBuildFinished) {
            mockkObject(JacocoTestKitReportCoveragePlugin) {
                justRun { gradle.dumpOnBuildFinished() }
                justRun { dumpCoverageData() }
                every { GradleVersion.current() } returns GradleVersion.version(gradleVersion)

                plugin.apply(mockk())

                verify {
                    if (flowAPI) {
                        gradle.dumpOnBuildFinished()

                    } else {
                        gradle.buildFinished(any<Action<BuildResult>>())
                        dumpCoverageData()
                    }
                }
            }
        }

    @Test
    fun `dumpCoverageData, when agent started, should dump coverage data`() = mockkStatic(RT::getAgent) {
        every { RT.getAgent() } returns agent

        dumpCoverageData()

        verify {
            RT.getAgent()
            agent.dump(true)
        }
    }

    @Test
    fun `dumpCoverageData, when agent is not initialized, should not fail`() = mockkStatic(RT::getAgent) {
        every { RT.getAgent() } throws IllegalStateException("JaCoCo agent not started.")

        dumpCoverageData()

        verify {
            RT.getAgent()
        }
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(gradle, agent)
    }

}
