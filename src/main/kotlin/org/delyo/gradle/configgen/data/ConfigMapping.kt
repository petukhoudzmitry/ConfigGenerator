package org.delyo.gradle.configgen.data

import org.delyo.gradle.configgen.extension.Language
import org.delyo.gradle.configgen.service.contract.Extractor
import java.io.File


open class ConfigMapping {
    var className: String = ""
    var packageName: String = ""
    val inputFiles: MutableSet<File> = mutableSetOf()
    var language: Language = Language.KOTLIN
    var extractors: MutableSet<Extractor> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ConfigMapping
        if (className != other.className) return false
        if (packageName != other.packageName) return false
        if (inputFiles != other.inputFiles) return false
        if (language != other.language) return false
        if (extractors != other.extractors) return false
        return true
    }

    override fun toString(): String {
        return "ConfigMapping(className=$className, packageName=$packageName, inputFiles=$inputFiles, language=$language, extractors=$extractors)"
    }

    override fun hashCode(): Int {
        var result = className.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + inputFiles.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + extractors.hashCode()
        return result
    }
}