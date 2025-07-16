plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.gitVersion)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    id("io.github.gmazzo.gradle.testkit.jacoco") version "+" // yeah, self reference to latest published version, but we need it for computing coverage of tests
}

group = "io.github.gmazzo.gradle.testkit.jacoco"
description = "Enables JaCoCo coverage collection for Gradle TestKit's GradleRunner tests"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

val originUrl = providers
    .exec { commandLine("git", "remote", "get-url", "origin") }
    .standardOutput.asText.map { it.trim() }

gradlePlugin {
    website = originUrl
    vcsUrl = originUrl

    plugins {
        create("jacoco-gradle-testkit") {
            id = "io.github.gmazzo.gradle.testkit.jacoco"
            displayName = name
            implementationClass = "io.github.gmazzo.gradle.testkit.jacoco.JacocoGradleTestKitPlugin"
            description = project.description
            tags.addAll("jacoco", "testkit", "offline", "jacoco-offline", "intrumentation", "gradle-runner")
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    pom {
        name = "${rootProject.name}-${project.name}"
        description = provider { project.description }
        url = originUrl

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
            }
        }

        developers {
            developer {
                id = "gmazzo"
                name = id
                email = "gmazzo65@gmail.com"
            }
        }

        scm {
            connection = originUrl
            developerConnection = originUrl
            url = originUrl
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

afterEvaluate {
    tasks.named<Jar>("javadocJar") {
        from(tasks.dokkaGeneratePublicationJavadoc)
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(tasks.publishPlugins)
}

tasks.publishPlugins {
    enabled = "$version".matches("\\d+(\\.\\d+)+".toRegex())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
