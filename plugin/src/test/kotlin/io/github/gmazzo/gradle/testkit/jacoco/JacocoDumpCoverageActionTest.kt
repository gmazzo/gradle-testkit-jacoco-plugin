package io.github.gmazzo.gradle.testkit.jacoco

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.jacoco.agent.rt.IAgent
import org.jacoco.agent.rt.RT
import org.junit.jupiter.api.Test

internal class JacocoDumpCoverageActionTest {

    val agent: IAgent = mockk(relaxUnitFun = true)

    val action = spyk<JacocoDumpCoverageAction>()

    @Test
    fun `execute, should dump coverage data`() = mockkStatic(RT::getAgent) {
        every { RT.getAgent() } returns agent

        action.execute(mockk())

        verify {
            agent.dump(true)
        }
    }


}