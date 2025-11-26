package org.delyo.gradle.configgen.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.delyo.gradle.configgen.util.uniqueSanitized
import java.io.File
import java.util.*


object KotlinGenerator : AbstractGenerator() {

    override fun generateCode(
        outputDir: File,
        packageRaw: String,
        classRaw: String,
        merged: Map<String, Any?>,
        inputFiles: Set<File>
    ) {
        val fileBuilder = FileSpec.builder(packageRaw, classRaw)
        val typeBuilder = TypeSpec.objectBuilder(classRaw)

        typeBuilder.addProperty(
            PropertySpec.builder("properties", Properties::class).addModifiers(KModifier.PRIVATE).build()
        )

        typeBuilder.addInitializerBlock(
            CodeBlock.builder().add("val files = listOf(")
                .apply { inputFiles.forEachIndexed { i, f -> if (i > 0) add(", "); add("%T(%S)", File::class, f.path) } }
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
}