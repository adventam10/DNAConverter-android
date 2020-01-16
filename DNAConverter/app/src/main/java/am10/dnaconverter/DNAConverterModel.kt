package am10.dnaconverter

import am10.dnaconverter.Extensions.*
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

enum class DownloadFileState {
    COMPLETED, UNWRITABLE, FAILED
}

interface DownloadFileCallBack {
    fun onDownloaded(result: DownloadFileState)
}

class DNAConverterModel(context: Context, private val downloadFileCallBack: DownloadFileCallBack) {

    val REQUEST_PERMISSION = 1000
    var originalText: String? = null
    var convertedText: String? = null

    private val dnaHexValues = mapOf(
        "AA" to "0", "AT" to "1", "AC" to "2", "AG" to "3",
        "TA" to "4", "TT" to "5", "TC" to "6", "TG" to "7",
        "CA" to "8", "CT" to "9", "CC" to "a", "CG" to "b",
        "GA" to "c", "GT" to "d", "GC" to "e", "GG" to "f"
    )
    private val hashTag: String = "&hashtags=" + context.getString(R.string.hash_tag).urlEncode()

    fun makeFileName(): String {
        val nowDate = SimpleDateFormat("yyyyMMddHHss").format(Date())
        return "$nowDate.txt"
    }

    fun getTwitterURL(): Uri? {
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

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    fun isAcceptedExternalStoragePermission(context: Context) : Boolean {
        return ActivityCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRequestExternalStoragePermission(activity: Activity) : Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun requestExternalStoragePermission(activity: Activity) {
        val reqPermissions = ArrayList<String>()
        reqPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(activity,
            reqPermissions.toTypedArray(), REQUEST_PERMISSION)
    }

    fun saveFile(context: Context, fileName: String, text: String) {
        if (!isExternalStorageWritable()) {
            downloadFileCallBack.onDownloaded(DownloadFileState.UNWRITABLE)
            return
        }
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)
        try {
            FileOutputStream(file, true).use{ fileOutputStream ->
                OutputStreamWriter(fileOutputStream, "UTF-8").use{ outputStreamWriter ->
                    BufferedWriter(outputStreamWriter).use{ bw ->
                        bw.write(text+"\n")
                        bw.flush()
                    }
                }
            }
            downloadFileCallBack.onDownloaded(DownloadFileState.COMPLETED)
        } catch (e: Exception) {
            downloadFileCallBack.onDownloaded(DownloadFileState.FAILED)
        }
    }
}