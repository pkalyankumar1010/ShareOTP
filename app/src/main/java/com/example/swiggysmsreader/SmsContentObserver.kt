package com.example.swiggysmsreader

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

class SmsContentObserver(private val context: Context, handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        
        if (uri == null) return

        // Check if the change is in the SMS content provider
        if (uri.toString().contains("content://sms")) {
             readLastSms()
        }
    }

    private fun readLastSms() {
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms"),
            null, null, null, "date DESC LIMIT 1"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val typeColumnIndex = it.getColumnIndex("type")
                val bodyColumnIndex = it.getColumnIndex("body")
                val addressColumnIndex = it.getColumnIndex("address")
                val dateColumnIndex = it.getColumnIndex("date")

                // Check if columns exist
                if (typeColumnIndex != -1 && bodyColumnIndex != -1 && addressColumnIndex != -1) {
                    val type = it.getInt(typeColumnIndex) // 1 = Inbox, 2 = Sent
                    val body = it.getString(bodyColumnIndex)
                    val address = it.getString(addressColumnIndex)
                    val timestamp = if (dateColumnIndex != -1) it.getLong(dateColumnIndex) else System.currentTimeMillis()

                    val sender = if (type == 2) "Me (Sent to $address)" else address
                    
                    // Post to our shared LiveData
                    SmsReceiver.receivedSms.postValue(SmsReceiver.SmsEvent(sender, body, timestamp))
                }
            }
        }
    }
}