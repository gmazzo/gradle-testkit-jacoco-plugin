package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
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
    internal val classesDirs: Provider<List<File>>

    @get:Classpath
    abstract val jacocoClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val instrumentedClassesDir: DirectoryProperty

    init {
        with(project) {
            classesDirs = classpath.elements.map { it.mapNotNull { it.asFile.takeIf(File::isDirectory) } }

            instrumentedClassesDir
                .convention(layout.dir(provider { temporaryDir.resolve("classes") }))
        }
    }

    @TaskAction
    fun transform(): Unit = with(ant) {
        invokeMethod(
            "taskdef",
            mapOf(
                "name" to "instrument",
                "classname" to "org.jacoco.ant.InstrumentTask",
                "classpath" to jacocoClasspath.asPath,
            )
        )

        withGroovyBuilder {
            "instrument"("destdir" to instrumentedClassesDir.get().asFile.absolutePath) {
                classesDirs.get().forEach {
                    "fileset"("dir" to it.absolutePath)
                }
            }
        }
    }

}
