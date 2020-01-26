package am10.dnaconverter.models

import android.content.Context
import org.json.JSONArray

class HistoryModel(context: Context) {
    companion object{
        const val PREF_HISTORIES = "DNAConvert_History"
        const val MAX_COUNT = 10
    }

    private val pref = context.getSharedPreferences(PREF_HISTORIES, Context.MODE_PRIVATE)
    val histories: Array<String>
        get() {
            val jsonArray = JSONArray(pref.getString(PREF_HISTORIES, "[]"))
            return Array(jsonArray.length()) {
                jsonArray.getString(it)
            }
        }

    fun addHistory(history: String) {
        var saveHistories = histories.toMutableList()
        val index = saveHistories.indexOfFirst { it == history }
        if (index != -1) {
            saveHistories.removeAt(index)
        }
        if (saveHistories.size >= MAX_COUNT) {
            saveHistories.removeAt(saveHistories.size - 1)
        }
        saveHistories.add(0, history)
        val jsonArray = JSONArray(saveHistories)
        pref.edit().putString(PREF_HISTORIES, jsonArray.toString()).apply()
    }
}