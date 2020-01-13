package am10.dnaconverter

import android.net.Uri
import java.net.URLEncoder

class DNAConverterModel {
    var originalText: String? = null
    var convertedText: String? = null
    val dnaHexValues = mapOf(
        "AA" to "0", "AT" to "1", "AC" to "2", "AG" to "3",
        "TA" to "4", "TT" to "5", "TC" to "6", "TG" to "7",
        "CA" to "8", "CT" to "9", "CC" to "a", "CG" to "b",
        "GA" to "c", "GT" to "d", "GC" to "e", "GG" to "f"
    )
    fun getTwitterURL(hashTag: String): Uri? {
        if (convertedText.isNullOrEmpty()) {
            return null
        }
        val url = "https://twitter.com/intent/tweet?text=" + urlEncode(convertedText!!) + hashTag
        return Uri.parse(url)
    }
    fun urlEncode(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }
    fun convertToDNA(text: String?): String? {
        if (text.isNullOrEmpty()) {
            return null
        }
        val hex = text.toByteArray().map { b -> String.format("%02X", b) }.joinToString("")
        var result = hex.toLowerCase()
        dnaHexValues.forEach { (k, v) -> result = result.replace(v, k) }
        return result
    }

    fun convertToLanguage(text: String?): String? {
        if (text.isNullOrEmpty()) {
            return null
        }
        if (isInvalidDNA(text)) {
            return null
        }
        var index = 0
        val strings: MutableList<String> = mutableListOf()
        while (index < text.length) {
            strings.add(text.substring(index, index+2))
            index += 2
        }
        val hex = strings.map { n -> dnaHexValues[n] }.joinToString("").toUpperCase()
        val result = ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        return String(result)
    }

    fun isInvalidDNA(text: String?): Boolean {
        if (text.isNullOrEmpty() || text.length%2!=0) {
            return true
        }
        if (Regex("""[ATCG]+""").matchEntire(text)?.value == null) {
            println("b: $text")
            return true
        }
        return false
    }
}