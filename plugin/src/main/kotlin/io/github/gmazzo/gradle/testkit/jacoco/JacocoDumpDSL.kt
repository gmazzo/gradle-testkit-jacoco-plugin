@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.flow.FlowScope
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.always
import org.gradle.kotlin.dsl.support.serviceOf

internal fun Gradle.dumpOnBuildFinished() {
    gradle.serviceOf<FlowScope>().always(JacocoDumpCoverageAction::class) { }
}
