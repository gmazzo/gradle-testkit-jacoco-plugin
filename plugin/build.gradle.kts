plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    id("io.github.gmazzo.gradle.testkit.jacoco") version "+" // yeah, self reference to latest published version, but we need it for computing coverage of tests
    signing
}

group = "io.github.gmazzo.gradle.testkit.jacoco"
description = "Gradle TestKit JaCoCo Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-testkit-jacoco-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-testkit-jacoco-plugin")

    plugins {
        create("jacoco-gradle-testkit") {
            id = "io.github.gmazzo.gradle.testkit.jacoco"
            displayName = name
            implementationClass = "io.github.gmazzo.gradle.testkit.jacoco.JacocoGradleTestKitPlugin"
            description = "Enables JaCoCo coverage collection for Gradle TestKit's GradleRunner tests"
            tags.addAll("jacoco", "testkit", "offline", "jacoco-offline", "intrumentation", "gradle-runner")
        }
    }
}

dependencies {
    val jacocoRuntime = variantOf(libs.jacoco) { classifier("runtime") }

    compileOnly(gradleKotlinDsl())
    compileOnly(jacocoRuntime)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.params)

    testImplementation(gradleKotlinDsl())
    testImplementation(gradleTestKit())
    testImplementation(libs.mockk)
    testImplementation(jacocoRuntime)
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useInMemoryPgpKeys(signingKey, signingPassword)
    publishing.publications.configureEach(::sign)
    tasks.withType<Sign>().configureEach { enabled = signingKey != null }
}

testing.suites.withType<JvmTestSuite> {
    useJUnitJupiter()
}

tasks.test {
    environment("TEMP_DIR", temporaryDir)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports.xml.required = true
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
