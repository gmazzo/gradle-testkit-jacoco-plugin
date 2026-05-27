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
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JacocoTestKitReportCoveragePluginTest {

    val gradle: GradleInternal = mockk {
        every { buildFinished(any<Action<BuildResult>>()) } answers {
            firstArg<Action<BuildResult>>().execute(mockk())
        }
    }

    val agent: IAgent = mockk(relaxUnitFun = true)

    val plugin = JacocoTestKitReportCoveragePlugin(gradle)

    fun testData() = listOf(
        of(MIN_GRADLE_VERSION),
        of(GradleVersion.version("9.0")),
        of(GradleVersion.current()),
    )

    @ParameterizedTest(name = "gradle={0}, flowAPI={1}")
    @MethodSource("testData")
    fun `should report coverage when build finishes`(gradleVersion: GradleVersion) =
        mockkStatic(GradleVersion::current, GradleInternal::dumpOnBuildFinished) {
            mockkObject(JacocoTestKitReportCoveragePlugin) {
                justRun { gradle.dumpOnBuildFinished() }
                justRun { dumpCoverageData() }
                every { GradleVersion.current() } returns gradleVersion

                plugin.apply(mockk())

                verify {
                    gradle.dumpOnBuildFinished()
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
