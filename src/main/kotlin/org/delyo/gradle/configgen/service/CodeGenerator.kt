package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.extension.Language
import java.io.File

object CodeGenerator {
    fun generateCode(outputDir: File, mapping: Map<Pair<String, String>, Set<ConfigMapping>>) {
        if (mapping.isNotEmpty()) {
            generateConfigPropertiesLoader(outputDir)
            mapping.forEach { (packageClass, configMappings) ->
                val language = configMappings.firstOrNull()?.language ?: return
                require(mapping.values.all { it.firstOrNull()?.language == language }) {
                    "All mappings for the same package and class must have same language"
                }

                when (language) {
                    Language.KOTLIN -> {
                        KotlinGenerator.generate(outputDir, packageClass, configMappings)
                    }

                    Language.JAVA -> {
                        JavaGenerator.generate(outputDir, packageClass, configMappings)
                    }
                }
            }
        }
    }

    private fun generateConfigPropertiesLoader(outputDir: File) {
        val file = File(outputDir, "org/delyo/buildconfig/runtime/ConfigPropertiesLoader.kt")
        file.parentFile.mkdirs()
        file.writeText(
            $$"""package org.delyo.buildconfig.runtime

import java.io.File
import java.nio.file.Files
import java.util.Properties

object ConfigPropertiesLoader {

    fun loadAsProperties(files: List<File>): Properties {
        val props = Properties()

        for (file in files) {
            if (!file.exists()) continue

            when (file.extension.lowercase()) {
                "yaml", "yml" -> {
                    val map = parseYaml(file)
                    flattenMapIntoProperties(map, "", props)
                }
                else -> {
                    Files.newBufferedReader(file.toPath()).use { reader ->
                        props.load(reader)
                    }
                }
            }
        }

        return props
    }

    private fun parseYaml(file: File): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        val stack = mutableListOf<Pair<Int, MutableMap<String, Any?>>>()
        stack.add(0 to result)
    
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
    
            val indent = line.indexOfFirst { !it.isWhitespace() }
            val keyValue = trimmed.split(":", limit = 2)
            val key = keyValue[0].trim()
            val value = keyValue.getOrNull(1)?.trim()
    
            val parentMap = run {
                while (stack.isNotEmpty() && stack.last().first >= indent) {
                    stack.removeAt(stack.lastIndex)
                }
                stack.lastOrNull()?.second ?: result
            }
    
            if (value == null || value.isEmpty()) {
                val newMap = linkedMapOf<String, Any?>()
                parentMap[key] = newMap
                stack.add(indent to newMap)
            } else {
                parentMap[key] = value
            }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
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
""".trimIndent()
        )
    }
}