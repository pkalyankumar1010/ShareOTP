package com.example.swiggysmsreader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.swiggysmsreader.databinding.FragmentTelegramConfigBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TelegramConfigFragment : Fragment() {

    private var _binding: FragmentTelegramConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelegramConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use EncryptedSharedPreferences
        val sharedPref = SecurePreferencesManager.getEncryptedSharedPreferences(requireContext())
        
        val savedBotToken = sharedPref.getString(getString(R.string.saved_telegram_bot_token_key), "")
        val savedChatId = sharedPref.getString(getString(R.string.saved_telegram_chat_id_key), "")

        binding.edittextBotToken.setText(savedBotToken)
        binding.edittextChatId.setText(savedChatId)

        binding.buttonSaveTelegram.setOnClickListener {
            val botToken = binding.edittextBotToken.text.toString()
            val chatId = binding.edittextChatId.text.toString()

            with(sharedPref.edit()) {
                putString(getString(R.string.saved_telegram_bot_token_key), botToken)
                putString(getString(R.string.saved_telegram_chat_id_key), chatId)
                apply()
            }
            
            Toast.makeText(context, "Configuration Saved Securely", Toast.LENGTH_SHORT).show()
        }

        binding.buttonTestTelegram.setOnClickListener {
            val botToken = binding.edittextBotToken.text.toString()
            val chatId = binding.edittextChatId.text.toString()
            
            if (botToken.isNotEmpty() && chatId.isNotEmpty()) {
                sendTelegramMessage(botToken, chatId, "Test message from ShareOTP")
            } else {
                Toast.makeText(context, "Please save configuration first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendTelegramMessage(botToken: String, chatId: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=${URLEncoder.encode(message, "UTF-8")}"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                
                val responseCode = conn.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        Toast.makeText(context, "Telegram Message Sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed: Error $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}