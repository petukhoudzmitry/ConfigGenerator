package org.delyo.configgen.api.services

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.services.contract.Extractor
import org.delyo.configgen.api.util.uniqueSanitized
import java.io.File
import java.util.*

@Suppress("UNCHECKED_CAST")
object KotlinGenerator : AbstractGenerator() {

    override fun generateCode(
        outputDir: File,
        packageRaw: String,
        classRaw: String,
        merged: Map<String, Any?>,
        fileToExtractionMap: Map<File, Pair<Extractor, ExtractionPolicy>>
    ) {
        val fileBuilder = FileSpec.builder(packageRaw, classRaw)
        val typeBuilder = TypeSpec.objectBuilder(classRaw)

        typeBuilder.addProperty(
            PropertySpec.builder("properties", Properties::class).addModifiers(KModifier.PRIVATE).build()
        )

        typeBuilder.addInitializerBlock(
            CodeBlock.builder().add("val filesToExtractionMap = mutableMapOf(")
                .apply {
                    var i = 0
                    fileToExtractionMap.forEach { file, (extractor, extractionPolicy) ->
                        if (i++ > 0) {
                            add(", ")
                        }
                        add(
                            "%T(%S) to %T(%T, %T.%L)",
                            File::class,
                            file.path,
                            Pair::class,
                            extractor::class,
                            ExtractionPolicy::class,
                            extractionPolicy.name
                        )
                    }
                }
                .add(")\n")
                .add(
                    "properties = %T.loadAsProperties(filesToExtractionMap)\n",
                    ClassName("org.delyo.loader", "RuntimeConfigLoader")
                )
                .build())

        generateForMap(merged, typeBuilder)

        fileBuilder.addType(typeBuilder.build())

        fileBuilder.build().writeTo(outputDir)
    }


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
}