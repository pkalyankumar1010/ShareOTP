package com.example.swiggysmsreader

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class SmsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString("sender") ?: return Result.failure()
        val message = inputData.getString("message") ?: return Result.failure()
        val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())

        return withContext(Dispatchers.IO) {
            try {
                // Use SecurePreferencesManager to get the encrypted shared preferences
                val sharedPref = SecurePreferencesManager.getEncryptedSharedPreferences(applicationContext)
                
                val headerRegexStr = sharedPref.getString(applicationContext.getString(R.string.saved_header_regex_key), "") ?: ""
                val messageRegexStr = sharedPref.getString(applicationContext.getString(R.string.saved_message_regex_key), "") ?: ""
                val botToken = sharedPref.getString(applicationContext.getString(R.string.saved_telegram_bot_token_key), "") ?: ""
                val chatId = sharedPref.getString(applicationContext.getString(R.string.saved_telegram_chat_id_key), "") ?: ""

                // Filter by Sender Name / Header
                if (headerRegexStr.isNotEmpty()) {
                    try {
                        val pattern = Pattern.compile(headerRegexStr)
                        if (!pattern.matcher(sender).matches()) {
                            return@withContext Result.success() // Ignored
                        }
                    } catch (e: Exception) {
                        Log.e("SmsWorker", "Invalid Header Regex: ${e.message}")
                    }
                }

                // Filter by Message Body
                if (messageRegexStr.isNotEmpty()) {
                    try {
                        val pattern = Pattern.compile(messageRegexStr)
                        if (!pattern.matcher(message).find()) {
                            return@withContext Result.success() // Ignored
                        }
                    } catch (e: Exception) {
                         Log.e("SmsWorker", "Invalid Message Regex: ${e.message}")
                    }
                }

                // Extract 6-digit OTP using Regex
                val otpPattern = Pattern.compile("(\\d{4,8})")
                val matcher = otpPattern.matcher(message)
                
                // Extract Amount after INR
                val amountPattern = Pattern.compile("INR\\s*([\\d,]+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
                val amountMatcher = amountPattern.matcher(message)
                var amountString: String? = null
                var amountValue: Double = 0.0
                
                if (amountMatcher.find()) {
                    amountString = amountMatcher.group(1)
                    try {
                        // Remove commas for parsing
                        amountValue = amountString?.replace(",", "")?.toDouble() ?: 0.0
                    } catch (e: NumberFormatException) {
                        Log.e("SmsWorker", "Error parsing amount: $amountString")
                    }
                }

                if (matcher.find()) {
                    val otp = matcher.group(1)
                    
                    // Send to Telegram
                    if (botToken.isNotEmpty() && chatId.isNotEmpty()) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                        val formattedDate = sdf.format(Date(timestamp))
                        
                        val sb = StringBuilder()
                        sb.append("Message from $sender\nTime: $formattedDate\nOTP: $otp")
                        
                        if (amountString != null) {
                            sb.append("\nAmount: INR $amountString")
                            
                            // Add emojis based on amount value
                            if (amountValue > 2000) {
                                sb.append(" 😡🤬💸")
                            } else if (amountValue < 200) {
                                sb.append(" 😃🎉💰")
                            } else {
                                sb.append(" 🙂")
                            }
                        }
                        
                        val telegramMsg = sb.toString()
                        sendToTelegram(botToken, chatId, telegramMsg)
                        
                        // Save to history (UI will refresh if observing database, but for now this saves it to pref)
                        SentHistoryManager.addHistoryItem(applicationContext, SentHistoryItem(
                            originalMessage = message,
                            telegramMessage = telegramMsg,
                            timestamp = timestamp,
                            sender = sender
                        ))
                    }
                }
                
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }

    private fun sendToTelegram(botToken: String, chatId: String, message: String) {
        val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=${URLEncoder.encode(message, "UTF-8")}"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connect()
        val response = conn.responseCode
        Log.d("SmsWorker", "Telegram response code: $response")
    }
}