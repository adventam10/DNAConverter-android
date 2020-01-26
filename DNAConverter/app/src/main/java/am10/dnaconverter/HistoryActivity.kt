package am10.dnaconverter

import am10.dnaconverter.models.HistoryModel
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_HISTORY = "history"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        val historyModel = HistoryModel(this)
        val listView = list_view_history
        val histories = historyModel.histories
        text_view_no_histories.visibility = if (histories.isEmpty()) View.VISIBLE else View.GONE
        val adapter = ArrayAdapter<String>(this,
            R.layout.list_history_row,
            R.id.text_view_history,
            histories)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val history = adapter.getItem(position)
            didSelectHistory(history!!)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun didSelectHistory(history: String) {
        val intent = Intent()
        intent.putExtra(EXTRA_HISTORY, history)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
