package org.delyo.gradle.configgen.data

import org.delyo.gradle.configgen.extension.Language
import org.delyo.gradle.configgen.service.contract.Extractor
import java.io.File


open class ConfigMapping {
    var className: String? = null
    var packageName: String? = null
    val inputFiles: MutableList<File> = mutableListOf()
    var language: Language = Language.KOTLIN
    var extractors: List<Extractor> = emptyList()
}