package am10.dnaconverter

import am10.dnaconverter.Extensions.*
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import android.os.Environment
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    enum class Mode {
        LANGUAGE, DNA
    }
    private val hashTag: String
        get() {
            return "&hashtags=" + getString(R.string.hash_tag).urlEncode()
        }

    private val REQUEST_PERMISSION = 1000

    private val mode: Mode
    get() {
        if (radio_group_mode.checkedRadioButtonId == R.id.radio_button_language) {
            return Mode.LANGUAGE
        }
        return Mode.DNA
    }
    private val model = DNAConverterModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_convert.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (mode == Mode.LANGUAGE) {
                text_view_converted.text = model.convertToDNA(model.originalText)?: getString(R.string.error_message)
            } else {
                text_view_converted.text = model.convertToLanguage(model.originalText)?: getString(R.string.error_message)
            }
        }

        image_button_twitter.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (model.isInvalidDNA(model.convertedText)) {
                showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_VIEW, model.getTwitterURL(hashTag))
            startActivity(intent)
        }

        image_button_download.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (model.isInvalidDNA(model.convertedText)) {
                showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
                return@setOnClickListener
            }
            checkPermission()
        }

        image_button_copy.setOnClickListener {
            hideKeyboard()
            setTexts()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.mode_dna), model.convertedText))
            showShortToast(getString(R.string.copy_message))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.share -> {
                shareConvertedText()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        saveFile(model.makeFileName(), model.convertedText!!)
                    } else {
                        showShortToast(getString(R.string.file_permission_denied_message))
                    }
                }
            }
        }
    }

    private fun shareConvertedText() {
        setTexts()
        if (model.isInvalidDNA(model.convertedText)) {
            showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
            return
        }
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, model.convertedText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun setTexts() {
        model.originalText = edit_text_original.text.toString()
        model.convertedText = text_view_converted.text.toString()
    }

    private fun saveFile(fileName: String, text: String) {
        if (!isExternalStorageWritable()) {
            showShortToast(getString(R.string.file_not_writable_message))
            return
        }
        val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
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
            showShortToast(getString(R.string.download_message))
        } catch (e: Exception) {
            e.printStackTrace()
            showShortToast(getString(R.string.file_download_failed_message))
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
           saveFile(model.makeFileName(), model.convertedText!!)
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val reqPermissions = ArrayList<String>()
            reqPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this,
                reqPermissions.toTypedArray(), REQUEST_PERMISSION)

        } else {
            showShortToast(getString(R.string.file_permission_denied_message))
            val reqPermissions = ArrayList<String>()
            reqPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this,
                reqPermissions.toTypedArray(), REQUEST_PERMISSION)
        }
    }
}
