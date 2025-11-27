package org.delyo.configgen.api.util

@Suppress("UNCHECKED_CAST")
fun deepMerge(target: MutableMap<String, Any?>, src: Map<String, Any?>) {
    for ((k, v) in src) {
        val existing = target[k]
        if (existing is MutableMap<*, *> && v is Map<*, *>) {
            deepMerge(existing as MutableMap<String, Any?>, v as Map<String, Any?>)
        } else {
            target[k] = when (v) {
                is Map<*, *> -> {
                    val copy = LinkedHashMap<String, Any?>()
                    for ((ck, cv) in v) {
                        copy[ck?.toString() ?: "null"] = if (cv is Map<*, *>) convertToMutableMap(cv) else cv
                    }
                    copy
                }

                else -> v
            }
        }
    }
}

fun convertToMutableMap(m: Map<*, *>): MutableMap<String, Any?> {
    val res = LinkedHashMap<String, Any?>()
    for ((k, v) in m) {
        res[k?.toString() ?: "null"] = if (v is Map<*, *>) convertToMutableMap(v) else v
    }
    return res
}

fun uniqueSanitized(raw: String, used: MutableSet<String>): String {
    var id = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
    id = id.replace(Regex("_+"), "_").trim('_')
    if (id.isEmpty()) id = "_"
    if (id.first().isDigit()) id = "_$id"
    val keywords = setOf("object", "class", "package", "val", "var", "fun", "if", "else", "when")
    if (keywords.contains(id)) id = "${id}_"
    var candidate = id
    var idx = 1
    while (used.contains(candidate)) {
        candidate = "${id}_$idx"; idx++
    }
    used.add(candidate)
    return candidate
}