package com.example.swiggysmsreader

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.swiggysmsreader.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var smsContentObserver: SmsContentObserver? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.RECEIVE_SMS] == true &&
                permissions[Manifest.permission.SEND_SMS] == true
            ) {
                Log.d("SmsReceiver", "Permissions granted")
                registerSmsObserver()
            } else {
                Log.d("SmsReceiver", "Permissions denied")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request SMS permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS
                )
            )
        } else {
            registerSmsObserver()
        }

        // Observe the LiveData from SmsReceiver to update UI ONLY
        SmsReceiver.receivedSms.observe(viewLifecycleOwner) { smsEvent ->
            if (smsEvent == null) return@observe

            val sender = smsEvent.sender
            val message = smsEvent.message
            
            // Just update UI if visible
            binding.textviewSender.text = sender
            
            val otpPattern = Pattern.compile("(\\d{4,8})")
            val matcher = otpPattern.matcher(message)
            
            if (matcher.find()) {
                val otp = matcher.group(1)
                binding.textviewOtp.text = otp
            } else {
                binding.textviewOtp.text = "No OTP"
            }
        }
    }

    private fun registerSmsObserver() {
        if (smsContentObserver == null) {
            smsContentObserver = SmsContentObserver(requireContext(), Handler(Looper.getMainLooper()))
            requireContext().contentResolver.registerContentObserver(
                Uri.parse("content://sms"),
                true,
                smsContentObserver!!
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        smsContentObserver?.let {
            requireContext().contentResolver.unregisterContentObserver(it)
            smsContentObserver = null
        }
        _binding = null
    }
}