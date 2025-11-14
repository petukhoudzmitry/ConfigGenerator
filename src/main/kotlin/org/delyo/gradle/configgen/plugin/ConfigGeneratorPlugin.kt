package org.delyo.gradle.configgen.plugin

import org.delyo.gradle.configgen.extension.ConfigGeneratorPluginExtension
import org.delyo.gradle.configgen.task.ConfigGeneratorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

class ConfigGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(
            ConfigGeneratorPluginExtension.NAME,
            ConfigGeneratorPluginExtension::class.java,
            target.objects
        )

        val taskProvider = target.tasks.register("generateConfig", ConfigGeneratorTask::class.java) { task ->
            task.configMappings.set(extension.configMappings)
            task.defaultClassName.set(extension.defaultClassName)
            task.outputPackage.set(extension.outputPackage)
            task.language.set(extension.language)
            task.outputDirectory.set(target.layout.buildDirectory.dir("generated/sources/configgen"))
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