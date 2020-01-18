package am10.dnaconverter.extensions

import java.net.URLEncoder

fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.splitInto(length: Int) : List<String> {
    var index = 0
    val strings: MutableList<String> = mutableListOf()
    while (index < this.length) {
        strings.add(this.substring(index, index+length))
        index += length
    }
    return strings
}

fun String.hexToString() : String {
    val result = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    return String(result)
}

fun String.hex() : String {
    return this.toByteArray().map { b -> String.format("%02X", b) }.joinToString("")
}

fun String.isOnlyStructuredBy(chars: String) : Boolean {
    if (Regex("""[$chars]+""").matchEntire(this)?.value == null) {
        return false
    }
    return true
}