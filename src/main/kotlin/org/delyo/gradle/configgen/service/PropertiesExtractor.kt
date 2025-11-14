package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.service.contract.Extractor
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.dataformat.javaprop.JavaPropsSchema
import java.io.File
import java.nio.file.Files
import java.util.*

class PropertiesExtractor : Extractor {
    override val extensions = listOf("properties")
    private val mapper = JavaPropsMapper()

    override fun extract(files: List<File>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        files.filter { it.exists() }.forEach { file ->
            val schema = JavaPropsSchema.emptySchema().withPathSeparator(".").withFirstArrayOffset(0)
            Files.newBufferedReader(file.toPath()).use { reader ->
                val properties = Properties()
                properties.load(reader)
                val parsed =
                    mapper.readPropertiesAs(properties, schema, Map::class.java) as? Map<String, String> ?: emptyMap()
                deepMerge(result, parsed)
            }
        }

        return result
    }

    private fun deepMerge(target: MutableMap<String, Any?>, source: Map<String, Any?>) {
        for ((key, value) in source) {
            val existing = target[key]
            if (existing is MutableMap<*, *> && value is Map<*, *>) {
                val merged = (existing as MutableMap<String, Any?>)
                deepMerge(merged, value as Map<String, Any?>)
                target[key] = merged
            } else if (existing is Map<*, *> && value is Map<*, *>) {
                val merged = existing.toMutableMap() as MutableMap<String, Any?>
                deepMerge(merged, value as Map<String, Any?>)
                target[key] = merged
            } else {
                target[key] = value
            }
        }
    }
}