package org.delyo.gradle.configgen.plugin

import org.delyo.gradle.configgen.extension.ConfigGeneratorPluginExtension
import org.delyo.gradle.configgen.task.ConfigGeneratorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Path

@Suppress("unused", "UNCHECKED_CAST")
class ConfigGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(
            ConfigGeneratorPluginExtension.NAME,
            ConfigGeneratorPluginExtension::class.java,
            target.objects
        )

        val taskName = "generateConfig"

        val taskProvider = target.tasks.register(taskName, ConfigGeneratorTask::class.java) { task ->
            task.defaultClassName.set(extension.defaultClassName)
            task.defaultPackageName.set(extension.defaultPackageName)
            task.defaultInputFiles.from(extension.inputFiles)
            task.defaultLanguage.set(extension.language)
            task.defaultExtractionPolicy.set(extension.defaultExtractionPolicy)
            task.configMappings.set(extension.configMappings)
            task.defaultExtractors.set(extension.extractors)
            task.defaultOutputDirectory.set(target.layout.buildDirectory.dir("generated/sources/configgen"))
            task.outputDirectory.set(target.layout.buildDirectory.dir("generated/sources/configgen"))
            task.inputFiles.from(extension.configMappings.orNull?.flatMap { it.inputFiles } ?: emptyList<Path>())
        }


        val group = "org.delyo"
        val version = "1.0.0"

        val apiArtifacts = listOf(
            "$group:common-api:$version",
            "$group:runtime-config-loader:$version"
        )

        val targetConfiguration = when {
            target.configurations.findByName("implementation") != null -> "implementation"
            target.configurations.findByName("api") != null -> "api"
            else -> "compile"
        }

        target.afterEvaluate {
            apiArtifacts.forEach { coord ->
                target.dependencies.add(targetConfiguration, coord)
            }
        }

        target.afterEvaluate { project ->

            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets.getByName("main")
            val generatedDir = File(target.layout.buildDirectory.orNull?.asFile?.path, "generated/sources/configgen")
            mainSourceSet.java.srcDir(generatedDir)
            target.tasks.named("compileJava").configure {
                it.dependsOn(taskProvider)
            }
            target.tasks.named("compileKotlin").configure {
                it.dependsOn(taskProvider)
            }
        }
    }
}