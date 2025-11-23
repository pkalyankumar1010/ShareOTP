package com.example.swiggysmsreader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SmsReceiver : BroadcastReceiver() {

    companion object {
        // Static LiveData that fragments can observe
        // Changed from MutableLiveData to SingleLiveEvent-like behavior or just clear it
        // For simplicity, we'll stick with MutableLiveData but handle consumption in the observer
        val receivedSms = MutableLiveData<SmsEvent?>()
    }
    
    data class SmsEvent(val sender: String, val message: String, val timestamp: Long, var handled: Boolean = false)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.originatingAddress ?: "Unknown"
                val body = message.messageBody ?: ""
                val timestamp = message.timestampMillis
                
                // 1. Trigger WorkManager for background processing
                val inputData = Data.Builder()
                    .putString("sender", sender)
                    .putString("message", body)
                    .putLong("timestamp", timestamp)
                    .build()
                    
                val request = OneTimeWorkRequestBuilder<SmsWorker>()
                    .setInputData(inputData)
                    .build()
                    
                WorkManager.getInstance(context).enqueue(request)

                // 2. Still post to UI for live updates if app is open
                receivedSms.postValue(SmsEvent(sender, body, timestamp))
            }
        }
    }
}