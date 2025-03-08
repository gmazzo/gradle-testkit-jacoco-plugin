package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withGroovyBuilder
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.gradle.testing.jacoco.plugins.JacocoPlugin.ANT_CONFIGURATION_NAME
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import java.io.File

class JacocoGradleTestKitPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "java-gradle-plugin")
        apply(plugin = "jacoco")

        val jacoco: JacocoPluginExtension by extensions

        val jacocoRuntime by configurations.registering {
            defaultDependencies {
                add(project.dependencies.create("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime"))
            }
        }

        val propertiesTask = tasks.register<JacocoAgentPropertiesTask>("generateJaCoCoAgentPropertiesForTestKit") {
            jacocoExecFile.set(tasks.getByName(TEST_TASK_NAME).the<JacocoTaskExtension>().destinationFile)
        }

        afterEvaluate {
            tasks.withType<PluginUnderTestMetadata>().configureEach task@{
                val original = files(pluginClasspath.from.toList())
                val outputDir = layout.buildDirectory.dir("jacoco/instrumented-classes/${this@task.name}")

                outputs.dir(outputDir)

                pluginClasspath.setFrom(
                    outputDir.map { instrumentWithJacoco(original, it.asFile) } ,
                    original, // for JAR or resources from the original classpath, the instrumented ones will take precedence
                    propertiesTask,
                    jacocoRuntime,
                    JacocoGradleTestKitPlugin::class.java.jarFile,
                )
            }
        }
    }

    private fun Project.instrumentWithJacoco(from: FileCollection, into: File) = with(createAntBuilder()) {
        invokeMethod(
            "taskdef",
            mapOf(
                "name" to "instrument",
                "classname" to "org.jacoco.ant.InstrumentTask",
                "classpath" to configurations.getByName(ANT_CONFIGURATION_NAME).asPath,
            )
        )
        withGroovyBuilder {
            "instrument"("destdir" to into) {
                from.forEach {
                    if (it.isDirectory && !it.startsWith(into.parentFile)) {
                        "fileset"("dir" to it, "includes" to "**/*.class")
                    }
                }
            }
        }
        return@with into
    }

    private val Class<*>.jarFile
        get() = protectionDomain.codeSource.location

}
