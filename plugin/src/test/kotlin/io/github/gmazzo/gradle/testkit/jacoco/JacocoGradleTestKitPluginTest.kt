package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

@TestInstance(PER_CLASS)
class JacocoGradleTestKitPluginTest {

    private val tempDir = File(System.getenv("TEMP_DIR"))

    fun testData() = listOf(
        of("7.0", "tmp/test/work/.gradle-test-kit/caches/jars-8/5c64ac5bc39bc8221bbf9cf22eff02ca/instrumentedPluginClasses.jar"),
        of("8.0", "tmp/test/work/.gradle-test-kit/caches/jars-9/f570f6b76063587474228731107f427c/instrumentedPluginClasses.jar"),
        of("8.1", "jacoco/instrumentedPluginClasses"),
        of(GradleVersion.current().version, "jacoco/instrumentedPluginClasses"),
    )

    @ParameterizedTest(name = "gradle={0}")
    @MethodSource("testData")
    fun `should instrument classes`(gradleVersion: String, pluginClasspath: String) {
        val projectDir = tempDir.resolve("project/gradle-${gradleVersion}").apply {
            deleteRecursively()
            File(this@JacocoGradleTestKitPluginTest.javaClass.getResource("/project")!!.path).copyRecursively(this)
        }

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withArguments("test", "-s")
            .build()

        val expectedClasspath = listOf(
            "$projectDir/build/jacoco/instrumentedPluginClasses",
            "$projectDir/build/classes/java/main",
            "$projectDir/build/resources/main",
            "$projectDir/build/tmp/generateJaCoCoAgentPropertiesForTestKit",
            "$tempDir/work/.gradle-test-kit/caches/modules-2/files-2.1/org.jacoco/org.jacoco.agent/0.8.12/2bec6efe140e3a38a81607181476a4016ef2c613/org.jacoco.agent-0.8.12-runtime.jar",
            "${tempDir.parentFile.parentFile}/$pluginClasspath",
        ).joinToString(separator = "\\:")

        assertEquals(
            """
            implementation-classpath=$expectedClasspath
            """.trimIndent().trim(),
            projectDir
                .resolve("build/pluginUnderTestMetadata/plugin-under-test-metadata.properties")
                .readText().trim()
        )

        assertEquals(
            listOf(
                "org/test/myplugin/utils/UtilsImpl.class",
                "org/test/myplugin/utils/Utils.class",
                "org/test/myplugin/MyPlugin.class",
            ),
            projectDir.resolve("build/jacoco/instrumentedPluginClasses").run root@{
                walkTopDown().filter { it.isFile }.map { it.toRelativeString(this@root) }.toList()
            })

        assertEquals(
            javaClass.getResource("/expected-coverage.xml")!!.readText().withoutSessionInfo,
            projectDir.resolve("build/reports/jacoco/test/jacocoTestReport.xml").readText().withoutSessionInfo,
        )
    }

    private val String.withoutSessionInfo
        get() = replace("<sessioninfo[^>]+/>".toRegex(), "")

}
