package org.delyo.gradle.configgen.service.contract

import java.io.File

interface Generator {
    fun generate(srcFile: File, outputDir: File, packageName: String, className: String, extracted: Map<String, Any?>)
}