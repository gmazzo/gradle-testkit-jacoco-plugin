![GitHub](https://img.shields.io/github/license/gmazzo/gradle-testkit-jacoco-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.gradle.testkit.jacoco)](https://plugins.gradle.org/plugin/io.github.gmazzo.gradle.testkit.jacoco)
[![Build Status](https://github.com/gmazzo/gradle-testkit-jacoco-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/gmazzo/gradle-testkit-jacoco-plugin/actions/workflows/build.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-testkit-jacoco-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-testkit-jacoco-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.gradle.testkit.jacoco+-repo:github.com/gmazzo/gradle-testkit-jacoco-plugin)

# gradle-testkit-jacoco-plugin
A Gradle plugin that enables JaCoCo coverage collection for Gradle TestKit's GradleRunner tests.
 
# Usage
Apply the plugin at the root project:
```kotlin
plugins {
    `java-gradle-plugin`
    id("io.github.gmazzo.gradle.testkit.jacoco") version "<latest>" 
}
```
