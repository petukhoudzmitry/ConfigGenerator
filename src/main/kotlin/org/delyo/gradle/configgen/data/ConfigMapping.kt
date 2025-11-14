package org.delyo.gradle.configgen.data

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject


open class ConfigMapping @Inject constructor(objects: ObjectFactory) {
    var inputFiles: ConfigurableFileCollection = objects.fileCollection()
    var className: String? = null
    var packageName: String? = null
    var language: String? = null
}