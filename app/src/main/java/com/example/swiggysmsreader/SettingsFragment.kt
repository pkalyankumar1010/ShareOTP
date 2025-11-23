package com.example.swiggysmsreader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.swiggysmsreader.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val savedHeaderRegex = sharedPref?.getString(getString(R.string.saved_header_regex_key), "")
        val savedMessageRegex = sharedPref?.getString(getString(R.string.saved_message_regex_key), "")

        binding.edittextHeaderRegex.setText(savedHeaderRegex)
        binding.edittextMessageRegex.setText(savedMessageRegex)

        binding.buttonSaveSettings.setOnClickListener {
            val headerRegex = binding.edittextHeaderRegex.text.toString()
            val messageRegex = binding.edittextMessageRegex.text.toString()

            with(sharedPref?.edit()) {
                this?.putString(getString(R.string.saved_header_regex_key), headerRegex)
                this?.putString(getString(R.string.saved_message_regex_key), messageRegex)
                this?.apply()
            }
            
            Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}