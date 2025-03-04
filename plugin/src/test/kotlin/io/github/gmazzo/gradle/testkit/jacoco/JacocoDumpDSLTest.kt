@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.gmazzo.gradle.testkit.jacoco

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowScope
import org.gradle.api.internal.GradleInternal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class JacocoDumpDSLTest {

    val flowScope: FlowScope = mockk {
        every { always<FlowParameters>(any(), any()) } returns mockk()
    }

    val gradle: GradleInternal = mockk {
        every { gradle } returns this
        every { services } returns mockk {
            every { get(FlowScope::class.java) } returns flowScope
        }
    }

    @Test
    fun `dumpOnBuildFinished, should hook on build flow`() {
        gradle.dumpOnBuildFinished()

        verify {
            flowScope.always(JacocoDumpCoverageAction::class.java, any())
        }
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(flowScope)
    }

}
