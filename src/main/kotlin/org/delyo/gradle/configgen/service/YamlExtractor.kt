package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.service.contract.Extractor
import tools.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import kotlin.collections.iterator

@Suppress("UNCHECKED_CAST")
class YamlExtractor : Extractor {
    override val extensions = listOf("yaml", "yml")
    private val mapper = YAMLMapper()

    override fun extract(files: List<File>): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()
        files.filter { it.exists() }.forEach { file ->
            val parsed = mapper.readValue(file, Map::class.java) as? Map<String, Any?> ?: mapOf()
            deepMerge(merged, parsed)
        }
        return merged
    }

    private fun deepMerge(target: MutableMap<String, Any?>, src: Map<String, Any?>) {
        for ((key, value) in src) {
            val existing = target[key]
            if (existing is MutableMap<*, *> && value is Map<*, *>) {
                deepMerge(existing as MutableMap<String, Any?>, value as Map<String, Any?>)
            } else {
                target[key] = value
            }
        }
    }
}