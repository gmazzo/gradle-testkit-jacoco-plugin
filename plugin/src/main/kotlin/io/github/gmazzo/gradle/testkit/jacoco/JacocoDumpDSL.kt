@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.flow.FlowScope
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.always
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.util.GradleVersion

internal val MIN_GRADLE_VERSION = GradleVersion.version("8.14")

internal fun Gradle.dumpOnBuildFinished() {
    gradle.serviceOf<FlowScope>().always(JacocoDumpCoverageAction::class) { }
}
