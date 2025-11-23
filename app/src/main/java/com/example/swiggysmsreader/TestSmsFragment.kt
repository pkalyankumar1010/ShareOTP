package com.example.swiggysmsreader

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.swiggysmsreader.databinding.FragmentTestSmsBinding

class TestSmsFragment : Fragment() {

    private var _binding: FragmentTestSmsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val savedPhone = sharedPref?.getString(getString(R.string.saved_phone_key), "")
        // Default message if nothing is saved
        val defaultMsg = "Your Swiggy OTP is 123456"
        val savedMsg = sharedPref?.getString(getString(R.string.saved_msg_key), defaultMsg)
        
        binding.edittextPhoneNumber.setText(savedPhone)
        binding.edittextSmsBody.setText(savedMsg)

        binding.buttonSendSms.setOnClickListener {
            val phone = binding.edittextPhoneNumber.text.toString()
            val message = binding.edittextSmsBody.text.toString()

            if (phone.isEmpty()) {
                Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save phone number and custom message
            with(sharedPref?.edit()) {
                this?.putString(getString(R.string.saved_phone_key), phone)
                this?.putString(getString(R.string.saved_msg_key), message)
                this?.apply()
            }

            sendSms(phone, message)
        }
    }

    private fun sendSms(phone: String, message: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission SEND_SMS not granted", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}