package org.delyo.configgen.api.services

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.services.contract.Extractor
import org.delyo.configgen.api.services.contract.Generator
import org.delyo.configgen.api.util.deepMerge
import java.io.File

abstract class AbstractGenerator : Generator {
    override fun generate(
        outputDir: File,
        packageClass: Pair<String, String>,
        configMappings: Set<ConfigMapping>
    ) {
        val (packageRaw, classRaw) = packageClass

        val merged: MutableMap<String, Any?> = LinkedHashMap()

        val filesToExtractionMap: MutableMap<File, Pair<Extractor, ExtractionPolicy>> = mutableMapOf()
        configMappings.forEach { configMapping ->
            configMapping.inputFiles.forEach { file ->
                val extractor = configMapping.extractors.firstOrNull { it.extensions.contains(file.extension) }
                requireNotNull(extractor) { "No extractor found for file ${file.name}" }

                filesToExtractionMap[file] = Pair(extractor, configMapping.extractionPolicy)
            }
        }

        val extractors = configMappings.flatMap { it.extractors }

        configMappings.groupBy { it.extractionPolicy }.forEach { (policy, mappings) ->
            val files = mappings.flatMap { it.inputFiles }
            val extractorsToFilesMap: Map<Extractor, List<File>> = extractors.associateWith { extractor ->
                files.filter { it.exists() }.filter { it.extension in extractor.extensions }
            }

            extractorsToFilesMap.forEach { (extractor, files) ->
                val extracted = when (policy) {
                    ExtractionPolicy.MERGE -> extractor.merge(files)
                    ExtractionPolicy.RETAIN -> extractor.retain(files)
                }

                deepMerge(merged, extracted)
            }
        }

        generateCode(outputDir, packageRaw, classRaw, merged, filesToExtractionMap)
    }
}