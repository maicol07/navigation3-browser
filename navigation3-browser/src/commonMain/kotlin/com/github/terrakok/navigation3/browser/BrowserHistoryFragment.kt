package com.github.terrakok.navigation3.browser

fun buildBrowserHistoryFragment(
    name: String,
    parameters: Map<String, String?> = emptyMap()
): String = buildString {
    append("#")
    append(encodeURIComponent(name))
    if (parameters.isNotEmpty()) {
        append("?")
        append(
            parameters.entries.sortedBy { it.key }.joinToString("&") {
                val k = encodeURIComponent(it.key)
                val v = it.value
                if (v == null) k else "$k=${encodeURIComponent(v)}"
            }
        )
    }
}

fun getBrowserHistoryFragmentName(fragment: String): String? {
    val res = fragment.substringAfter('#', "").substringBeforeLast('?')
    if (res.isBlank() || res.contains('#') || res.contains('?')) return null
    return decodeURIComponent(res)
}

fun getBrowserHistoryFragmentParameters(fragment: String): Map<String, String?> {
    val paramStr = fragment.substringAfterLast('?', "")
    if (paramStr.isEmpty()) return emptyMap()
    return paramStr.split("&").filter { it.isNotEmpty() }.associate { p ->
        val split = p.split("=", limit = 2)
        val key = split[0]
        val value = split.getOrNull(1)
        decodeURIComponent(key) to value?.let { decodeURIComponent(value) }
    }
}