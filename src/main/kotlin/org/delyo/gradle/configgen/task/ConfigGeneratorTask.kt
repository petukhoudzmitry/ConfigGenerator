package org.delyo.gradle.configgen.task

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.extension.Language
import org.delyo.gradle.configgen.service.KotlinGenerator
import org.delyo.gradle.configgen.service.PropertiesExtractor
import org.delyo.gradle.configgen.service.YamlExtractor
import org.delyo.gradle.configgen.service.contract.Extractor
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
@CacheableTask
open class ConfigGeneratorTask @Inject constructor(@Internal val objects: ObjectFactory) : DefaultTask() {
    @Internal
    val kotlinGenerator = KotlinGenerator()

    @get:Input
    val defaultClassName: Property<String> = objects.property(String::class.java)

    @get:Input
    val defaultPackageName: Property<String> = objects.property(String::class.java)

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val inputFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    val language: Property<Language> = objects.property(Language::class.java)

    @Internal
    val configMappings: ListProperty<ConfigMapping> = objects.listProperty(ConfigMapping::class.java)

    @Internal
    val extractors: ListProperty<Extractor> = objects.listProperty(Extractor::class.java)

    @get:OutputDirectory
    val outputDirectory = objects.directoryProperty()

    init {
        group = "build"
        description = "Generate config classes"
    }

    @TaskAction
    fun generate() {
        processBaseInputFiles()
        processMappings()
    }

    private fun processMappings() {
        if (configMappings.orNull.isNullOrEmpty()) {
            return
        }

        val mappings = configMappings.get()

        mappings.filter {
            it.inputFiles.isNotEmpty()
        }.forEach {
            val className = it.className ?: defaultClassName.orNull ?: return@forEach
            val packageName = it.packageName ?: defaultPackageName.orNull ?: return@forEach
            val inputFiles = it.inputFiles.toList()
            val extractors: List<Extractor> =
                it.extractors.ifEmpty { extractors.orNull ?: listOf(PropertiesExtractor(), YamlExtractor()) }

            val extracted = inputFiles.map { file ->
                extractors.first { extractor ->
                    extractor.extensions.contains(file.extension)
                }.extract(listOf(file))
            }

            when (it.language) {
                Language.KOTLIN -> {
                    extracted.forEach { map ->
                        kotlinGenerator.generate(
                            project.projectDir.resolve("src/main/resources/config.yaml"),
                            outputDirectory.get().asFile,
                            packageName,
                            className,
                            map
                        )
                    }
                }

                Language.JAVA -> {
//                    TODO: generate java classes
                }
            }
        }
    }

    private fun processBaseInputFiles() {
        if (inputFiles.isEmpty ||
            defaultClassName.orNull.isNullOrEmpty() ||
            defaultPackageName.orNull.isNullOrEmpty() ||
            language.orNull == null || extractors.orNull.isNullOrEmpty()
        ) {
            return
        }

        val extractors = extractors.orNull ?: listOf(PropertiesExtractor(), YamlExtractor())

        val extracted = inputFiles.map { file ->
            extractors.first {
                it.extensions.contains(file.extension)
            }.extract(listOf(file))
        }

        when (language.orNull ?: Language.KOTLIN) {
            Language.KOTLIN -> {
                extracted.forEach { map ->
                    kotlinGenerator.generate(
                        project.projectDir.resolve("local.properties"),
                        outputDirectory.get().asFile,
                        defaultPackageName.get(),
                        defaultClassName.get(),
                        map
                    )
                }
            }

            Language.JAVA -> {
//                TODO: generate java classes
            }
        }
    }
}