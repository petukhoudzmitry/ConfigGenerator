package org.delyo.configgen.api.services

import org.delyo.configgen.api.data.ConfigMapping
import org.delyo.configgen.api.enums.ExtractionPolicy
import org.delyo.configgen.api.services.contract.Extractor
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
        fileToExtractionMap: Map<File, Pair<Extractor, ExtractionPolicy>>
    ) {
        TODO("Not yet implemented")
    }
}