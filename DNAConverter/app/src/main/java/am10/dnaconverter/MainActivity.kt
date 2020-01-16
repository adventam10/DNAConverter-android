package am10.dnaconverter

import am10.dnaconverter.Extensions.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.content.pm.PackageManager
import kotlinx.android.synthetic.main.activity_main.*

enum class ConvertMode {
    LANGUAGE, DNA
}

class MainActivity : AppCompatActivity(), DownloadFileCallBack {

    private lateinit var model: DNAConverterModel
    private val mode: ConvertMode
    get() {
        if (radio_group_mode.checkedRadioButtonId == R.id.radio_button_language) {
            return ConvertMode.LANGUAGE
        }
        return ConvertMode.DNA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = DNAConverterModel(this, this)
        button_convert.setOnClickListener {
            hideKeyboard()
            convertDNA()
        }
        image_button_twitter.setOnClickListener {
            hideKeyboard()
            tweetDNA()
        }
        image_button_download.setOnClickListener {
            hideKeyboard()
            downloadDNA()
        }
        image_button_copy.setOnClickListener {
            hideKeyboard()
            copyConvertedText()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                shareConvertedText()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == model.REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        model.saveFile(this, model.makeFileName(), model.convertedText!!)
                    } else {
                        showShortToast(getString(R.string.file_permission_denied_message))
                    }
                }
            }
        }
    }

    override fun onDownloaded(result: DownloadFileState) {
        val message = when (result) {
            DownloadFileState.COMPLETED -> getString(R.string.download_message)
            DownloadFileState.UNWRITABLE -> getString(R.string.file_not_writable_message)
            DownloadFileState.FAILED -> getString(R.string.file_download_failed_message)
        }
        showShortToast(message)
    }

    private fun convertDNA() {
        setTexts()
        if (mode == ConvertMode.LANGUAGE) {
            text_view_converted.text = model.convertToDNA(model.originalText)?: getString(R.string.error_message)
        } else {
            text_view_converted.text = model.convertToLanguage(model.originalText)?: getString(R.string.error_message)
        }
    }

    private fun tweetDNA() {
        setTexts()
        if (model.isInvalidDNA(model.convertedText)) {
            showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, model.getTwitterURL())
        startActivity(intent)
    }

    private fun downloadDNA() {
        setTexts()
        if (model.isInvalidDNA(model.convertedText)) {
            showConfirmationDialog(getString(R.string.alert_title), getString(R.string.alert_message))
            return
        }
        if (model.isAcceptedExternalStoragePermission(this)) {
            model.saveFile(this, model.makeFileName(), model.convertedText!!)
        } else {
            requestExternalStoragePermission()
        }
    }

    private fun copyConvertedText() {
        setTexts()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.mode_dna), model.convertedText))
        showShortToast(getString(R.string.copy_message))
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

    private fun requestExternalStoragePermission() {
        if (model.shouldShowRequestExternalStoragePermission(this)) {
            model.requestExternalStoragePermission(this)
        } else {
            showShortToast(getString(R.string.file_permission_denied_message))
            model.requestExternalStoragePermission(this)
        }
    }
}
