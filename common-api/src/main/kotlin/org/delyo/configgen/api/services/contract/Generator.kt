package org.delyo.configgen.api.services.contract

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.ExtractionPolicy
import java.io.File

interface Generator {
    fun generate(outputDir: File, packageClass: Pair<String, String>, configMappings: Set<ConfigMapping>)
    fun generateCode(
        outputDir: File,
        packageRaw: String,
        classRaw: String,
        merged: Map<String, Any?>,
        fileToExtractionMap: Map<File, Pair<Extractor, ExtractionPolicy>>
    )
}