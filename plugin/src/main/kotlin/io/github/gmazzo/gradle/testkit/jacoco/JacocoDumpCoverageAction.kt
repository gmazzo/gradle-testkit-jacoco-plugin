@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.gradle.testkit.jacoco

import io.github.gmazzo.gradle.testkit.jacoco.JacocoTestKitReportCoveragePlugin.Companion.dumpCoverageData
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters

internal abstract class JacocoDumpCoverageAction : FlowAction<FlowParameters.None> {

    override fun execute(parameters: FlowParameters.None) {
        dumpCoverageData()
    }

}
