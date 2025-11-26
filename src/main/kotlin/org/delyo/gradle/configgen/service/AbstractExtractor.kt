package org.delyo.gradle.configgen.service

import org.delyo.gradle.configgen.service.contract.Extractor

abstract class AbstractExtractor : Extractor {
    protected fun generateUniqueName(name: String, names: Set<String>) : String {
        var i = 1
        var newName = name
        while (newName in names) {
            newName = "${name}_$i"
            i++
        }
        return newName
    }
}