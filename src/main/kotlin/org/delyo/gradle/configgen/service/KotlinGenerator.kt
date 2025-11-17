package org.delyo.gradle.configgen.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.delyo.gradle.configgen.data.ConfigMapping
import org.delyo.gradle.configgen.service.contract.Extractor
import java.io.File
import java.util.*

object KotlinGenerator : AbstractGenerator() {
    override fun generate(
        outputDir: File,
        packageClass: Pair<String, String>,
        configMappings: Set<ConfigMapping>
    ) {
        val (packageRaw, classRaw) = packageClass

        val merged: MutableMap<String, Any?> = LinkedHashMap()

        val extractors: MutableSet<Extractor> = mutableSetOf()
        extractors.addAll(configMappings.flatMap { it.extractors })

        val files: MutableSet<File> = mutableSetOf()
        files.addAll(configMappings.flatMap { it.inputFiles })
        val extractorsToFilesMap: Map<Extractor, List<File>> = extractors.associateWith { extractor ->
            files.filter { file -> extractor.extensions.contains(file.extension) }
        }

        for ((extractor, files) in extractorsToFilesMap) {
            val extracted = extractor.extract(files)
            deepMerge(merged, extracted)

        }

        generateCode(outputDir, packageRaw, classRaw, merged, files)
    }

    @Suppress("UNCHECKED_CAST")
    private fun deepMerge(target: MutableMap<String, Any?>, src: Map<String, Any?>) {
        for ((k, v) in src) {
            val existing = target[k]
            if (existing is MutableMap<*, *> && v is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                deepMerge(existing as MutableMap<String, Any?>, v as Map<String, Any?>)
            } else {
                target[k] = when (v) {
                    is Map<*, *> -> {
                        val copy = LinkedHashMap<String, Any?>()
                        for ((ck, cv) in v) {
                            copy[ck?.toString() ?: "null"] = if (cv is Map<*, *>) convertToMutableMap(cv) else cv
                        }
                        copy
                    }

                    else -> v
                }
            }
        }
    }

    private fun convertToMutableMap(m: Map<*, *>): MutableMap<String, Any?> {
        val res = LinkedHashMap<String, Any?>()
        for ((k, v) in m) {
            res[k?.toString() ?: "null"] = if (v is Map<*, *>) convertToMutableMap(v) else v
        }
        return res
    }

    private fun generateCode(
        outputDir: File,
        packageName: String,
        className: String,
        merged: Map<String, Any?>,
        files: Set<File>
    ) {
        val fileBuilder = FileSpec.builder(packageName, className)
        val typeBuilder = TypeSpec.objectBuilder(className)

        typeBuilder.addProperty(
            PropertySpec.builder("properties", Properties::class).addModifiers(KModifier.PRIVATE).build()
        )

        typeBuilder.addInitializerBlock(
            CodeBlock.builder().add("val files = listOf(")
            .apply { files.forEachIndexed { i, f -> if (i > 0) add(", "); add("%T(%S)", File::class, f.path) } }
            .add(")\n")
            .add(
                "properties = %T.loadAsProperties(files)\n",
                ClassName("org.delyo.buildconfig.runtime", "ConfigPropertiesLoader")
            )
            .build())

        generateForMap(merged, typeBuilder)

        fileBuilder.addType(typeBuilder.build())

        fileBuilder.build().writeTo(outputDir)
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateForMap(
        map: Map<String, Any?>,
        parentBuilder: TypeSpec.Builder,
        pathPrefix: String = ""
    ) {
        val usedNames = mutableSetOf<String>()
        for ((key, value) in map) {
            val nestedName = uniqueSanitized(key, usedNames)

            val fullPath = if (pathPrefix.isEmpty()) key else "$pathPrefix.$key"

            when (value) {
                is Map<*, *> -> {
                    val nestedBuilder = TypeSpec.objectBuilder(nestedName)
                    generateForMap(value as Map<String, Any?>, nestedBuilder, fullPath)
                    parentBuilder.addType(nestedBuilder.build())
                }

                is List<*> -> {
                    val listType = ClassName("kotlin.collections", "List")
                        .parameterizedBy(ClassName("kotlin", "String"))
                    val getter = FunSpec.getterBuilder()
                        .addStatement("return properties[%S] as String", fullPath)
                        .build()
                    parentBuilder.addProperty(
                        PropertySpec.builder(nestedName, listType)
                            .getter(getter)
                            .build()
                    )
                }

                else -> {
                    val getter = FunSpec.getterBuilder()
                        .addStatement("return properties[%S] as String", fullPath)
                        .build()
                    parentBuilder.addProperty(
                        PropertySpec.builder(nestedName, String::class)
                            .getter(getter)
                            .build()
                    )
                }
            }
        }
    }

    private fun uniqueSanitized(raw: String, used: MutableSet<String>): String {
        var id = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
        id = id.replace(Regex("_+"), "_").trim('_')
        if (id.isEmpty()) id = "_"
        if (id.first().isDigit()) id = "_$id"
        val keywords = setOf("object", "class", "package", "val", "var", "fun", "if", "else", "when")
        if (keywords.contains(id)) id = "${id}_"
        var candidate = id
        var idx = 1
        while (used.contains(candidate)) {
            candidate = "${id}_$idx"; idx++
        }
        used.add(candidate)
        return candidate
    }
}