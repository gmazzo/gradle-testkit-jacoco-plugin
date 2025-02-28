package io.github.gmazzo.gradle.testkit.jacoco

import io.github.gmazzo.gradle.testkit.jacoco.JacocoTestKitReportCoveragePlugin.Companion.dumpCoverageData
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class JacocoDumpCoverageActionTest {

    val action = spyk<JacocoDumpCoverageAction>()

    @Test
    fun `execute, should call dumpCoverageData`() = mockkObject(JacocoTestKitReportCoveragePlugin) {
        justRun { dumpCoverageData() }

        action.execute(mockk())

        verify {
            dumpCoverageData()
        }
    }

}
