package io.github.gmazzo.gradle.testkit.jacoco

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowScope
import org.gradle.api.internal.GradleInternal
import org.gradle.util.GradleVersion
import org.jacoco.agent.rt.IAgent
import org.jacoco.agent.rt.RT
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class JacocoTestKitReportCoveragePluginTest {

    val flowScope: FlowScope = mockk {
        every { always<FlowParameters>(any(), any()) } returns mockk()
    }

    val gradle: GradleInternal = mockk {
        every { gradle } returns this
        every { services } returns mockk {
            every { get(FlowScope::class.java) } returns flowScope
        }
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
        mockkStatic(GradleVersion::current, RT::getAgent) {
            every { RT.getAgent() } returns agent
            every { GradleVersion.current() } returns GradleVersion.version(gradleVersion)

            plugin.apply(mockk())

            verify {
                if (flowAPI) {
                    flowScope.always(JacocoDumpCoverageAction::class.java, any())

                } else {
                    agent.dump(true)
                }
            }
            confirmVerified(agent, flowScope)
        }

}
