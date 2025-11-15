package org.delyo.gradle.configgen.service.contract

abstract class AbstractGenerator : Generator {
    fun safePropertyName(name: String): String {
        return name.replace('.', '_').replace('-', '_')
    }
}