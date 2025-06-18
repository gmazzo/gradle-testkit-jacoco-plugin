package io.github.gmazzo.gradle.testkit.jacoco

import java.io.File
import java.util.Properties
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class JacocoAgentPropertiesTaskTest(
    @TempDir private val projectDir: File
) {

    private val project = ProjectBuilder.builder()
        .withProjectDir(projectDir)
        .build()

    private val execFile = projectDir.resolve("build/jacoco/aFile.exec")

    private val outDir = projectDir.resolve("build/generated/resources")

    private val task = project.tasks.register<JacocoAgentPropertiesTask>("testTask") {
        jacocoExecFile.set(execFile)
        generatedResourcesDir.set(outDir)
    }

    @Test
    fun `should generate jacoco agent properties file`() {
        task.get().generateAgentProperties()

        val actual = outDir.resolve("jacoco-agent.properties").reader(Charsets.UTF_8).use {
            Properties().apply { load(it) }
        }

        assertEquals(
            mapOf("destfile" to execFile.absolutePath),
            actual,
        )
    }

}
