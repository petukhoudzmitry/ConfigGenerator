package org.delyo.gradle.configgen.task

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.enums.Language
import org.delyo.configgen.api.services.CodeGenerator
import org.delyo.configgen.api.services.PluginInputNormalizer
import org.delyo.configgen.api.services.contract.Extractor
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

    @get:Input
    val defaultExtractionPolicy: Property<ExtractionPolicy> = objects.property(ExtractionPolicy::class.java)

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
        val merged = PluginInputNormalizer.normalize(
            configMappings,
            defaultClassName,
            defaultPackageName,
            defaultExtractors,
            defaultInputFiles,
            defaultLanguage
        )
        CodeGenerator.generateCode(outputDirectory.get().asFile, merged)
    }
}