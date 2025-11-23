package com.example.swiggysmsreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<SentHistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSender: TextView = view.findViewById(R.id.text_sender)
        val textTimestamp: TextView = view.findViewById(R.id.text_timestamp)
        val textOriginal: TextView = view.findViewById(R.id.text_original_msg)
        val textTelegram: TextView = view.findViewById(R.id.text_telegram_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.textSender.text = "Sender: ${item.sender}"
        holder.textTimestamp.text = SentHistoryManager.formatTimestamp(item.timestamp)
        holder.textOriginal.text = "Original SMS: ${item.originalMessage}"
        holder.textTelegram.text = "Telegram Msg: ${item.telegramMessage}"
    }

    override fun getItemCount() = historyList.size
}