package org.delyo.gradle.configgen.service.contract

import org.delyo.gradle.configgen.data.ConfigMapping
import java.io.File

interface Generator {
    fun generate(outputDir: File, packageClass: Pair<String, String>, configMappings: Set<ConfigMapping>)
}