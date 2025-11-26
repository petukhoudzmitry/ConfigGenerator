package org.delyo.configgen.api.services.contract

import org.delyo.configgen.api.data.ConfigMapping
import java.io.File

interface Generator {
    fun generate(outputDir: File, packageClass: Pair<String, String>, configMappings: Set<ConfigMapping>)
    fun generateCode(
        outputDir: File,
        packageRaw: String,
        classRaw: String,
        merged: Map<String, Any?>,
        inputFiles: Set<File>
    )
}