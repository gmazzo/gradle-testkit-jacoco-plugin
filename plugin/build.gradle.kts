plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
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
    compileOnly(gradleKotlinDsl())
    compileOnly(variantOf(libs.jacoco) { classifier("runtime") })

    testImplementation(gradleKotlinDsl())
    testImplementation(gradleTestKit())
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.params)
}

testing.suites.withType<JvmTestSuite> {
    useJUnitJupiter()
}

tasks.check {
    dependsOn(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoReport> {
    reports.xml.required = true
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
