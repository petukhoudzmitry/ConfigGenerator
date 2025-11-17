package org.delyo.gradle.configgen.task

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.extension.Language
import org.delyo.gradle.configgen.service.CodeGenerator
import org.delyo.gradle.configgen.service.contract.Extractor
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
@CacheableTask
open class ConfigGeneratorTask @Inject constructor(@Internal val objects: ObjectFactory) : DefaultTask() {

    @get:Input
    val defaultClassName: Property<String> = objects.property(String::class.java)

    @get:Input
    val defaultPackageName: Property<String> = objects.property(String::class.java)

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val defaultInputFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val inputFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    val defaultLanguage: Property<Language> = objects.property(Language::class.java)

    @Internal
    val configMappings: ListProperty<ConfigMapping> = objects.listProperty(ConfigMapping::class.java)

    @Internal
    val defaultExtractors: ListProperty<Extractor> = objects.listProperty(Extractor::class.java)

    @get:OutputDirectory
    val defaultOutputDirectory = objects.directoryProperty()

    @get:OutputDirectory
    val outputDirectory = objects.directoryProperty()

    init {
        group = "build"
        description = "Generate config classes"
    }

    @TaskAction
    fun generate() {
        val merged = mergeInputsPerPackageAndClass()
        CodeGenerator.generateCode(outputDirectory.get().asFile, merged)
    }

    private fun mergeInputsPerPackageAndClass(): Map<Pair<String, String>, Set<ConfigMapping>> {
        val result = mutableMapOf<Pair<String, String>, Set<ConfigMapping>>()
        val packageClasses = mutableSetOf<Pair<String, String>>()

        val configMappings =
            configMappings.orNull?.filter { it.inputFiles.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        configMappings.add(generateConfigMappingFromDefaults())

        val processedConfigMappings = configMappings.map {
            if (it.className.isEmpty()) {
                it.className = defaultClassName.orNull ?: ""
            }
            if (it.packageName.isEmpty()) {
                it.packageName = defaultPackageName.orNull ?: ""
            }
            if (it.extractors.isEmpty()) {
                it.extractors = defaultExtractors.orNull?.toMutableSet() ?: mutableSetOf()
            }
            it
        }.filter { it.className.isNotEmpty() && it.packageName.isNotEmpty() && it.extractors.isNotEmpty() }

        packageClasses.addAll(processedConfigMappings.map { Pair(it.packageName, it.className) })
        packageClasses.remove(Pair("", ""))

        packageClasses.forEach { packageClass ->
            val matching = processedConfigMappings.filter {
                it.packageName == packageClass.first && it.className == packageClass.second
            }.ifEmpty {
                emptySet()
            }.toSet()

            result[packageClass] = matching
        }

        return result
    }

    private fun generateConfigMappingFromDefaults(): ConfigMapping {
        return ConfigMapping().apply {
            className = defaultClassName.orNull ?: ""
            packageName = defaultPackageName.orNull ?: ""
            inputFiles.addAll(defaultInputFiles.files.toList())
            language = defaultLanguage.orNull ?: Language.KOTLIN
            extractors = defaultExtractors.orNull?.toMutableSet() ?: mutableSetOf()
        }
    }
}