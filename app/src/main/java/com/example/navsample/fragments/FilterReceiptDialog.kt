package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.FilterReceiptBinding

class FilterReceiptDialog(
    var text: String,
    var returnChange: (String) -> Unit
) : DialogFragment() {
    private var _binding: FilterReceiptBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FilterReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInput.setText(text)

        binding.textLayout.setEndIconOnClickListener {
            binding.textInput.setText(text)
        }

        binding.confirmButton.setOnClickListener {
            returnChange.invoke(binding.textInput.text.toString())
            dismiss()
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }
}