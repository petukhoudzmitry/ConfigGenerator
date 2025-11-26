package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.extension.ExtractionPolicy
import org.delyo.gradle.configgen.service.contract.Extractor
import org.delyo.gradle.configgen.service.contract.Generator
import org.delyo.gradle.configgen.util.deepMerge
import java.io.File

abstract class AbstractGenerator : Generator {
    override fun generate(
        outputDir: File,
        packageClass: Pair<String, String>,
        configMappings: Set<ConfigMapping>
    ) {
        val (packageRaw, classRaw) = packageClass

        val merged: MutableMap<String, Any?> = LinkedHashMap()

        val files: MutableSet<File> = mutableSetOf()
        files.addAll(configMappings.flatMap { it.inputFiles })

        val extractors = configMappings.flatMap { it.extractors }.toSet()

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

        generateCode(outputDir, packageRaw, classRaw, merged, files)
    }
}