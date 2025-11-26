package org.delyo.configgen.api.services

import org.delyo.configgen.api.util.deepMerge
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.dataformat.javaprop.JavaPropsSchema
import java.io.File
import java.nio.file.Files
import java.util.*

@Suppress("UNCHECKED_CAST")
class PropertiesExtractor : AbstractExtractor() {
    override val extensions = listOf("properties")
    private val mapper = JavaPropsMapper()

    override fun merge(files: List<File>): Map<String, Any?> {
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

    override fun retain(files: List<File>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        val names = mutableSetOf<String>()

        files.filter { it.exists() }.forEach { file ->
            val schema = JavaPropsSchema.emptySchema().withPathSeparator(".").withFirstArrayOffset(0)

            val name = generateUniqueName(file.nameWithoutExtension, names)
            names.add(name)

            Files.newBufferedReader(file.toPath()).use { reader ->
                val properties = Properties()
                properties.load(reader)
                val parsed =
                    mapper.readPropertiesAs(properties, schema, Map::class.java) as? Map<String, Any?> ?: emptyMap()
                val retained = mutableMapOf<String, Any?>(name to parsed)
                deepMerge(result, retained)
            }
        }
        return result
    }
}