package org.delyo.configgen.api.services

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.Language
import java.io.File

object CodeGenerator {
    fun generateCode(outputDir: File, mapping: Map<Pair<String, String>, Set<ConfigMapping>>) {
        if (mapping.isNotEmpty()) {
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
}