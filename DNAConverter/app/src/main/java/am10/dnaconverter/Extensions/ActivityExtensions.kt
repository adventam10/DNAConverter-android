package am10.dnaconverter.Extensions

import am10.dnaconverter.R
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.hideKeyboard() {
    val windowToken = currentFocus?.windowToken
    if (windowToken != null) {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun AppCompatActivity.showConfirmationDialog(title: String, message: String) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(getString(R.string.alert_positive_button_title), null).show()
}

fun AppCompatActivity.showShortToast(message: String) {
    Toast.makeText(this , message, Toast.LENGTH_SHORT).show();
}