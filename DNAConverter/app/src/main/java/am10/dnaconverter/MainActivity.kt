package am10.dnaconverter

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import android.os.Environment
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    enum class Mode {
        LANGUAGE, DNA
    }
    private val hashTag: String
        get() {
            return "&hashtags=" + model.urlEncode(getString(R.string.hash_tag))
        }

    private val REQUEST_PERMISSION = 1000
    private val convertedTextView: TextView
    get() {
        return findViewById<TextView>(R.id.text_view_converted)!!
    }
    private val originalEditText: EditText
        get() {
            return findViewById<EditText>(R.id.edit_text_original)!!
        }
    private val mode: Mode
    get() {
        val radioGroup: RadioGroup = findViewById(R.id.radio_group_mode)
        if (radioGroup.checkedRadioButtonId == R.id.radio_button_language) {
            return Mode.LANGUAGE
        }
        return Mode.DNA
    }
    private val model = DNAConverterModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val convertButton: Button = findViewById(R.id.button_convert)
        convertButton.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (mode == Mode.LANGUAGE) {
                convertedTextView.text = model.convertToDNA(model.originalText)?: getString(R.string.error_message)
            } else {
                convertedTextView.text = model.convertToLanguage(model.originalText)?: getString(R.string.error_message)
            }
        }
        val twitterButton: ImageButton = findViewById(R.id.image_button_twitter)
        twitterButton.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (model.isInvalidDNA(model.convertedText)) {
                showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_VIEW, model.getTwitterURL(hashTag))
            startActivity(intent)
        }
        val downloadButton: ImageButton = findViewById(R.id.image_button_download)
        downloadButton.setOnClickListener {
            hideKeyboard()
            setTexts()
            if (model.isInvalidDNA(model.convertedText)) {
                showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
                return@setOnClickListener
            }
            checkPermission()
        }
        val copyButton: ImageButton = findViewById(R.id.image_button_copy)
        copyButton.setOnClickListener {
            hideKeyboard()
            setTexts()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.mode_dna), model.convertedText))
            Toast.makeText(this , R.string.copy_message, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, getString(R.string.file_permission_denied_message), Toast.LENGTH_SHORT).show()
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

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setTexts() {
        model.originalText = originalEditText.text.toString()
        model.convertedText = convertedTextView.text.toString()
    }

    private fun showConfirmationDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.alert_positive_button_title), null).show()
    }

    private fun saveFile(fileName: String, text: String) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this , R.string.file_not_writable_message, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this , R.string.download_message, Toast.LENGTH_SHORT).show();
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this , getString(R.string.file_download_failed_message), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.file_permission_denied_message), Toast.LENGTH_SHORT).show()
            val reqPermissions = ArrayList<String>()
            reqPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this,
                reqPermissions.toTypedArray(), REQUEST_PERMISSION)
        }
    }
}
