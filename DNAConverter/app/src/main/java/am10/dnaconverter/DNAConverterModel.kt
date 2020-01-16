package am10.dnaconverter

import am10.dnaconverter.Extensions.*
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*


class DNAConverterModel {
    var originalText: String? = null
    var convertedText: String? = null
    val dnaHexValues = mapOf(
        "AA" to "0", "AT" to "1", "AC" to "2", "AG" to "3",
        "TA" to "4", "TT" to "5", "TC" to "6", "TG" to "7",
        "CA" to "8", "CT" to "9", "CC" to "a", "CG" to "b",
        "GA" to "c", "GT" to "d", "GC" to "e", "GG" to "f"
    )

    fun makeFileName(): String {
        val nowDate = SimpleDateFormat("yyyyMMddHHss").format(Date())
        return nowDate!! + ".txt"
    }

    fun getTwitterURL(hashTag: String): Uri? {
        if (convertedText.isNullOrEmpty()) {
            return null
        }
        val url = "https://twitter.com/intent/tweet?text=" + convertedText!!.urlEncode() + hashTag
        return Uri.parse(url)
    }

    fun convertToDNA(text: String?): String? {
        if (text.isNullOrEmpty()) {
            return null
        }
        var result = text.hex().toLowerCase()
        dnaHexValues.forEach { (k, v) -> result = result.replace(v, k) }
        return result
    }

    fun convertToLanguage(text: String?): String? {
        if (text.isNullOrEmpty() || isInvalidDNA(text)) {
            return null
        }
        val hex = text.splitInto(2).map { n -> dnaHexValues[n] }.joinToString("").toUpperCase()
        return hex.hexToString()
    }

    fun isInvalidDNA(text: String?): Boolean {
        if (text.isNullOrEmpty() || text.length%2!=0) {
            return true
        }
        if (!text.isOnlyStructuredBy("ATCG")) {
            return true
        }
        return false
    }
}