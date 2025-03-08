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
import java.util.*

@TestInstance(PER_CLASS)
class JacocoGradleTestKitPluginTest {

    private val tempDir = File(System.getenv("TEMP_DIR"))

    fun testData() = listOf(
        of("7.0"),
        of("8.0"),
        of("8.1"),
        of(GradleVersion.current().version),
    )

    @ParameterizedTest(name = "gradle={0}")
    @MethodSource("testData")
    fun `should instrument classes`(gradleVersion: String) {
        val projectDir = tempDir.resolve("project/gradle-${gradleVersion}").apply {
            deleteRecursively()
            File(this@JacocoGradleTestKitPluginTest.javaClass.getResource("/project")!!.path).copyRecursively(this)
        }

        projectDir.resolve("build.gradle").appendText("""
            file("self-jar.txt").text = file(${JacocoGradleTestKitPlugin::class.qualifiedName}.class.protectionDomain.codeSource.location)
        """.trimIndent())

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withArguments("test", "-s")
            .build()

        val actualClasspath = with(Properties()) {
            projectDir.resolve("build/pluginUnderTestMetadata/plugin-under-test-metadata.properties")
                .reader().use(::load)
            getProperty("implementation-classpath").split(':')
        }

        assertEquals(
            listOf(
                "$projectDir/build/jacoco/instrumented-classes/pluginUnderTestMetadata",
                "$projectDir/build/classes/java/main",
                "$projectDir/build/resources/main",
                "$tempDir/work/.gradle-test-kit/caches/modules-2/files-2.1/com.squareup/javapoet/1.13.0/d6562d385049f35eb50403fa86bb11cce76b866a/javapoet-1.13.0.jar",
                "$projectDir/build/tmp/generateJaCoCoAgentPropertiesForTestKit",
                "$tempDir/work/.gradle-test-kit/caches/modules-2/files-2.1/org.jacoco/org.jacoco.agent/0.8.12/2bec6efe140e3a38a81607181476a4016ef2c613/org.jacoco.agent-0.8.12-runtime.jar",
                projectDir.resolve("self-jar.txt").readText(),
            ),
            actualClasspath
        )

        assertEquals(
            setOf(
                "org/test/myplugin/utils/UtilsImpl.class",
                "org/test/myplugin/utils/Utils.class",
                "org/test/myplugin/MyPlugin.class",
                ".jacoco-instrumented",
            ),
            projectDir.resolve("build/jacoco/instrumented-classes/pluginUnderTestMetadata").run root@{
                walkTopDown().filter { it.isFile }.map { it.toRelativeString(this@root) }.toSet()
            })

        assertEquals(
            javaClass.getResource("/expected-coverage.xml")!!.readText().withoutSessionInfo,
            projectDir.resolve("build/reports/jacoco/test/jacocoTestReport.xml").readText().withoutSessionInfo,
        )
    }

    private val String.withoutSessionInfo
        get() = replace("<sessioninfo[^>]+/>".toRegex(), "")

}
