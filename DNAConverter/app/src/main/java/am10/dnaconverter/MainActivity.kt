package am10.dnaconverter

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

class MainActivity : AppCompatActivity() {

    enum class Mode {
        LANGUAGE, DNA
    }
    val hashTag: String
        get() {
            return "&hashtags=" + model.urlEncode(getString(R.string.hash_tag))
        }

    val convertedTextView: TextView
    get() {
        return findViewById<TextView>(R.id.text_view_converted)!!
    }
    val originalEditText: EditText
        get() {
            return findViewById<EditText>(R.id.edit_text_original)!!
        }
    val mode: Mode
    get() {
        val radioGroup: RadioGroup = findViewById(R.id.radio_group_mode)
        if (radioGroup.checkedRadioButtonId == R.id.radio_button_language) {
            return Mode.LANGUAGE
        }
        return Mode.DNA
    }
    val model = DNAConverterModel()

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
            saveFile("test.txt", model.convertedText!!)
            Toast.makeText(this , R.string.download_message, Toast.LENGTH_SHORT).show();
        }
        val copyButton: ImageButton = findViewById(R.id.image_button_copy)
        copyButton.setOnClickListener {
            hideKeyboard()
            setTexts()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.mode_dna), model.convertedText))
            Toast.makeText(this , R.string.copy_message, Toast.LENGTH_SHORT).show();
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

    private fun saveFile(file: String, text: String) {
        applicationContext.openFileOutput(file, Context.MODE_PRIVATE).use {
            it.write(text.toByteArray())
        }
    }
}
