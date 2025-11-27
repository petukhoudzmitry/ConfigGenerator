package org.delyo.loader

import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.services.contract.Extractor
import org.delyo.configgen.api.util.deepMerge
import java.io.File
import java.util.*

@Suppress("UNCHECKED_CAST")
object RuntimeConfigLoader {

    fun loadAsProperties(filesToExtractionMap: Map<File, Pair<Extractor, ExtractionPolicy>>): Properties {

        val merged = mutableMapOf<String, Any?>()

        filesToExtractionMap.forEach { file, (extractor, extractionPolicy) ->
            val extracted = when (extractionPolicy) {
                ExtractionPolicy.MERGE -> extractor.merge(listOf(file))
                ExtractionPolicy.RETAIN -> extractor.retain(listOf(file))
            }
            deepMerge(merged, extracted)
        }

        val props = Properties()

        flattenMapIntoProperties(merged, "", props)

        return props
    }

    private fun flattenMapIntoProperties(
        map: Map<String, Any?>,
        prefix: String,
        props: Properties
    ) {
        for ((k, v) in map) {
            val key = if (prefix.isEmpty()) k else "$prefix.$k"
            when (v) {
                is Map<*, *> -> flattenMapIntoProperties(v as Map<String, Any?>, key, props)
                is List<*> -> props.setProperty(key, v.filterNotNull().joinToString(","))
                null -> {}
                else -> props.setProperty(key, v.toString())
            }
        }
    }
}
