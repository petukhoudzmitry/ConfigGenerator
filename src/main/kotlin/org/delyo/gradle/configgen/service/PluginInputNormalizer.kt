package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.extension.Language
import org.delyo.gradle.configgen.service.contract.Extractor
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

object PluginInputNormalizer {
    fun normalize(
        configMappings: ListProperty<ConfigMapping>,
        defaultClassName: Property<String>,
        defaultPackageName: Property<String>,
        defaultExtractors: ListProperty<Extractor>,
        defaultInputFiles: ConfigurableFileCollection,
        defaultLanguage: Property<Language>,
    ): Map<Pair<String, String>, Set<ConfigMapping>> {
        val result = mutableMapOf<Pair<String, String>, Set<ConfigMapping>>()
        val packageClasses = mutableSetOf<Pair<String, String>>()

        val configMappings =
            configMappings.orNull?.filter { it.inputFiles.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        configMappings.add(
            generateConfigMappingFromDefaults(
                defaultClassName,
                defaultPackageName,
                defaultInputFiles,
                defaultLanguage,
                defaultExtractors
            )
        )

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

    private fun generateConfigMappingFromDefaults(
        defaultClassName: Property<String>,
        defaultPackageName: Property<String>,
        defaultInputFiles: ConfigurableFileCollection,
        defaultLanguage: Property<Language>,
        defaultExtractors: ListProperty<Extractor>
    ): ConfigMapping {
        return ConfigMapping().apply {
            className = defaultClassName.orNull ?: ""
            packageName = defaultPackageName.orNull ?: ""
            inputFiles.addAll(defaultInputFiles.files.toList())
            language = defaultLanguage.orNull ?: Language.KOTLIN
            extractors = defaultExtractors.orNull?.toMutableSet() ?: mutableSetOf()
        }
    }
}