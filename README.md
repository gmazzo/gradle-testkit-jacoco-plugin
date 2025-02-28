![GitHub](https://img.shields.io/github/license/gmazzo/gradle-testkit-jacoco-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.gradle.testkit.jacoco)](https://plugins.gradle.org/plugin/io.github.gmazzo.gradle.testkit.jacoco)
[![Build Status](https://github.com/gmazzo/gradle-testkit-jacoco-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/gmazzo/gradle-testkit-jacoco-plugin/actions/workflows/build.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-testkit-jacoco-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-testkit-jacoco-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.gradle.testkit.jacoco+-repo:github.com/gmazzo/gradle-testkit-jacoco-plugin)

# gradle-testkit-jacoco-plugin
A Gradle plugin that enables JaCoCo coverage collection for Gradle TestKit's GradleRunner tests.
 
# Usage
Apply the plugin at your `java-gradle-plugin` project:
```kotlin
plugins {
    `java-gradle-plugin`
    id("io.github.gmazzo.gradle.testkit.jacoco") version "<latest>" 
}
```

Then, on each test using [`GradleRunner`](https://docs.gradle.org/current/javadoc/org/gradle/testkit/runner/GradleRunner.html), 
also apply the `jacoco-testkit-coverage` plugin at either root's `build.gradle` or `settings.gradle`:
```kotlin
class MyPluginFunctionalTest {
    
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    
    @Test
    fun myPluginTest() {
        temporaryFolder.root.resolve("settings.gradle").writeText(
            """
            plugins {
                id("jacoco-testkit-coverage") // this will dump coverage data
            }

            rootProject.name = "test-project"
            """.trimIndent()
        )
        
        val result = GradleRunner.create()
            .withProjectDir(temporaryFolder.root)
            .withPluginClasspath()
            .withArguments("myTask")
            .build()
    }
}
```
> [!NOTE]
> The `jacoco-testkit-coverage` is an internal plugin added by this TestKit extension.
> It can be either applied to the root `Project`'s script or to the `Settings` script

# How it works
The plugin modifies [`java-gradle-plugin`](https://docs.gradle.org/current/userguide/java_gradle_plugin.html)'s
`pluginUnderTestMetadata` classpath (the one computed by [`GradleRunner.withPluginClasspath`](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.testkit.runner/-gradle-runner/with-plugin-classpath.html)
by using [`JaCoCo`'s Offline Instrumentation](https://www.jacoco.org/jacoco/trunk/doc/offline.html)

Also, a `jacoco-testkit-coverage` plugin also added to `withPluginClasspath`, allowing to dump the coverage data after each build is run.
