package org.delyo.gradle.configgen.extension

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.service.DefaultExtractor
import org.delyo.gradle.configgen.service.contract.Extractor
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
open class ConfigGeneratorPluginExtension @Inject constructor(objects: ObjectFactory) {
    companion object {
        const val NAME = "configGen"
    }

    var inputFiles: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    var extractor: Property<Class<out Extractor>> =
        (objects.property(Class::class.java) as Property<Class<out Extractor>>)
            .convention(DefaultExtractor::class.java as Class<out Extractor>)
    var defaultClassName: Property<String> = objects.property(String::class.java).convention("BuildConfig")
    var outputPackage: Property<String> = objects.property(String::class.java).convention("com.generated.config")
    var language: Property<String> = objects.property(String::class.java).convention("kotlin")
    var configMappings: ListProperty<ConfigMapping> = objects.listProperty(ConfigMapping::class.java)


}