package org.delyo.gradle.configgen.service.contract

import java.io.File

interface Extractor {
    val extensions: List<String>
    fun extract(files: List<File>): Map<String, Any?>
}