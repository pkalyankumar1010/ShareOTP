package com.example.swiggysmsreader

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SentHistoryItem(
    val originalMessage: String,
    val telegramMessage: String,
    val timestamp: Long,
    val sender: String
)

object SentHistoryManager {
    private const val PREF_NAME = "sent_history_pref"
    private const val KEY_HISTORY = "history_list"

    fun addHistoryItem(context: Context, item: SentHistoryItem) {
        val history = getHistory(context).toMutableList()
        history.add(0, item) // Add to top
        saveHistory(context, history)
    }

    fun getHistory(context: Context): List<SentHistoryItem> {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sharedPref.getString(KEY_HISTORY, null) ?: return emptyList()
        
        val type = object : TypeToken<List<SentHistoryItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveHistory(context: Context, history: List<SentHistoryItem>) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(history)
        with(sharedPref.edit()) {
            putString(KEY_HISTORY, json)
            apply()
        }
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}