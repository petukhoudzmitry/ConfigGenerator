package org.delyo.gradle.configgen.service.contract

import java.io.File

interface Extractor {
    val extensions: List<String>
    fun merge(files: List<File>) : Map<String, Any?>
    fun retain(files: List<File>) : Map<String, Any?>
}