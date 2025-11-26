package org.delyo.configgen.api.services

import org.delyo.configgen.api.util.deepMerge
import tools.jackson.dataformat.yaml.YAMLMapper
import java.io.File

@Suppress("UNCHECKED_CAST")
class YamlExtractor : AbstractExtractor() {
    override val extensions = listOf("yaml", "yml")
    private val mapper = YAMLMapper()

    override fun merge(files: List<File>): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()
        files.filter { it.exists() }.forEach { file ->
            val parsed = mapper.readValue(file, Map::class.java) as? Map<String, Any?> ?: mapOf()
            deepMerge(merged, parsed)
        }
        return merged
    }

    override fun retain(files: List<File>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        val names = mutableSetOf<String>()

        files.filter { it.exists() }.forEach { file ->
            val name = generateUniqueName(file.nameWithoutExtension, names)
            names.add(name)

            val parsed = mapper.readValue(file, Map::class.java) as? Map<String, Any?> ?: emptyMap()
            val retained = mutableMapOf<String, Any?>(name to parsed)
            deepMerge(result, retained)
        }
        return result
    }
}