package am10.dnaconverter

import am10.dnaconverter.extensions.*
import am10.dnaconverter.models.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

enum class ConvertMode {
    LANGUAGE, DNA
}

class MainActivity : AppCompatActivity() {

    private lateinit var originalEditText: EditText
    private lateinit var convertedTextView: TextView
    private lateinit var model: DNAConverterModel
    private val fileDownloadModel = FileDownloadModel()
    private lateinit var appUpdateModel: AppUpdateModel
    private val mode: ConvertMode
    get() {
        return when (radio_group_mode.checkedRadioButtonId) {
            R.id.radio_button_language -> ConvertMode.LANGUAGE
            else -> ConvertMode.DNA
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        originalEditText = edit_text_original
        convertedTextView = text_view_converted
        appUpdateModel = AppUpdateModel(this)
        appUpdateModel.checkAppVersion(this) {
            popupSnackbarForCompleteUpdate()
        }
        model = DNAConverterModel(this)
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
        button_clear.setOnClickListener {
            hideKeyboard()
            clearTexts()
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateModel.addOnCompleteListener {
            popupSnackbarForCompleteUpdate()
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
        if (requestCode == fileDownloadModel.REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        fileDownloadModel.saveFile(this, model.makeFileName(), model.convertedText!!) {
                            onDownloaded(it)
                        }
                    } else {
                        showShortToast(getString(R.string.file_permission_denied_message))
                    }
                }
            }
        }
    }

    private fun onDownloaded(result: DownloadFileState) {
        val message = when (result) {
            DownloadFileState.COMPLETED -> getString(R.string.download_message)
            DownloadFileState.UNWRITABLE -> getString(R.string.file_not_writable_message)
            DownloadFileState.FAILED -> getString(R.string.file_download_failed_message)
        }
        showShortToast(message)
    }

    private fun clearTexts() {
        originalEditText.setText("", TextView.BufferType.NORMAL)
        convertedTextView.text = ""
        setTexts()
    }

    private fun convertDNA() {
        setTexts()
        if (mode == ConvertMode.LANGUAGE) {
            convertedTextView.text = model.convertToDNA(model.originalText)?: getString(R.string.error_message)
        } else {
            convertedTextView.text = model.convertToLanguage(model.originalText)?: getString(R.string.error_message)
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
        if (fileDownloadModel.isAcceptedExternalStoragePermission(this)) {
            fileDownloadModel.saveFile(this, model.makeFileName(), model.convertedText!!) {
                onDownloaded(it)
            }
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
        model.originalText = originalEditText.text.toString()
        model.convertedText = convertedTextView.text.toString()
    }

    private fun requestExternalStoragePermission() {
        if (fileDownloadModel.shouldShowRequestExternalStoragePermission(this)) {
            fileDownloadModel.requestExternalStoragePermission(this)
        } else {
            showShortToast(getString(R.string.file_permission_denied_message))
            fileDownloadModel.requestExternalStoragePermission(this)
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(findViewById(R.id.root_layout),
            getString(R.string.update_completed_message), Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.update_action_title)) {
                appUpdateModel.completeUpdate()
            }
            .show()
    }
}
