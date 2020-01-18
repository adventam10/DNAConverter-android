package am10.dnaconverter.models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.ArrayList

enum class DownloadFileState {
    COMPLETED, UNWRITABLE, FAILED
}

class FileDownloadModel {
    val REQUEST_PERMISSION = 1000

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

    fun saveFile(context: Context, fileName: String, text: String, callback: ((DownloadFileState) -> (Unit))?) {
        if (!isExternalStorageWritable()) {
            callback?.invoke(DownloadFileState.UNWRITABLE)
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
            callback?.invoke(DownloadFileState.COMPLETED)
        } catch (e: Exception) {
            callback?.invoke(DownloadFileState.FAILED)
        }
    }
}