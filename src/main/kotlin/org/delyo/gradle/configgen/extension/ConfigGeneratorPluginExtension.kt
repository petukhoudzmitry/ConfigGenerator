package org.delyo.gradle.configgen.extension

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.enums.Language
import org.delyo.configgen.api.services.PropertiesExtractor
import org.delyo.configgen.api.services.YamlExtractor
import org.delyo.configgen.api.services.contract.Extractor
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
open class ConfigGeneratorPluginExtension @Inject constructor(val objects: ObjectFactory) {
    companion object {
        const val NAME = "configGen"
    }

    val defaultClassName: Property<String> = objects.property(String::class.java).convention("BuildConfig")
    val defaultPackageName: Property<String> = objects.property(String::class.java).convention("com.generated.config")
    val inputFiles: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val language: Property<Language> = objects.property(Language::class.java).convention(Language.KOTLIN)
    val defaultExtractionPolicy: Property<ExtractionPolicy> = objects.property(ExtractionPolicy::class.java).convention(
        ExtractionPolicy.RETAIN
    )
    val configMappings: ListProperty<ConfigMapping> = objects.listProperty(ConfigMapping::class.java)
    val extractors: ListProperty<Extractor> = objects.listProperty(Extractor::class.java).convention(
        listOf(
            PropertiesExtractor, YamlExtractor
        )
    )

    fun map(configure: ConfigMapping.() -> Unit) {
        val configMap = objects.newInstance(ConfigMapping::class.java)
        configMap.configure()
        configMappings.add(configMap)
    }
}