package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.data.ConfigMapping
import java.io.File

object JavaGenerator : AbstractGenerator() {
    override fun generate(
        outputDir: File,
        packageClass: Pair<String, String>,
        configMappings: Set<ConfigMapping>
    ) {
        TODO("Not yet implemented")
    }

    override fun generateCode(
        outputDir: File,
        packageRaw: String,
        classRaw: String,
        merged: Map<String, Any?>,
        inputFiles: Set<File>
    ) {
        TODO("Not yet implemented")
    }
}