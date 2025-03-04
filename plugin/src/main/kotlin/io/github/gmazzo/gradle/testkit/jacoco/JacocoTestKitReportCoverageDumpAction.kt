package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowScope
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.always
import org.gradle.kotlin.dsl.support.serviceOf
import org.jacoco.agent.rt.RT

internal abstract class DumpAction : FlowAction<FlowParameters.None> {

    override fun execute(parameters: FlowParameters.None) {
        runCatching { RT.getAgent() } // it may not be started of no instrumented classes are run
            .getOrNull()?.dump(true)
    }


    companion object {

        fun Gradle.dumpOnBuildFinished() {
            gradle.serviceOf<FlowScope>().always(DumpAction::class) { }
        }

    }

}
