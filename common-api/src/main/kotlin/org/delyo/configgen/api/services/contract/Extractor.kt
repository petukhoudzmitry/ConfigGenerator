package org.delyo.configgen.api.services.contract

import java.io.File

interface Extractor {
    val extensions: List<String>
    fun merge(files: List<File>) : Map<String, Any?>
    fun retain(files: List<File>) : Map<String, Any?>
}