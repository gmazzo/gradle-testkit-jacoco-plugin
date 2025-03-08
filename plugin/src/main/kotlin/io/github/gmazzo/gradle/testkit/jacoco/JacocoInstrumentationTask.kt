package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File

@CacheableTask
abstract class JacocoInstrumentationTask : DefaultTask() {

    @get:Internal
    abstract val classpath: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val classesDirs =
        classpath.elements.map { it.mapNotNull { it.asFile.takeIf(File::isDirectory) } }

    @get:Classpath
    abstract val jacocoClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val instrumentedClassesDir: DirectoryProperty

    @TaskAction
    fun transform(): Unit = with(ant) {
        val outDir = instrumentedClassesDir.get().asFile

        outDir.deleteRecursively()
        outDir.mkdirs()
        outDir.instrumentedMarker.createNewFile()

        invokeMethod(
            "taskdef",
            mapOf(
                "name" to "instrument",
                "classname" to "org.jacoco.ant.InstrumentTask",
                "classpath" to jacocoClasspath.asPath,
            )
        )

        withGroovyBuilder {
            "instrument"("destdir" to outDir) {
                classesDirs.get().forEach {
                    if (!it.instrumentedMarker.isFile) {
                        "fileset"("dir" to it, "includes" to "**/*.class")
                    }
                }
            }
        }
    }

    private val File.instrumentedMarker
        get() = resolve(".jacoco-instrumented")

}
