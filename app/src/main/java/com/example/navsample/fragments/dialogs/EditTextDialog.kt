package com.example.navsample.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogEditTextBinding

class EditTextDialog(
    var text: String,
    var onStartClick: () -> Unit,
    var onEndClick: () -> Unit,
    var returnChange: (String) -> Unit
) : DialogFragment() {

    constructor(text: String, returnChange: (String) -> Unit) : this(text, {}, {}, returnChange)

    private var _binding: DialogEditTextBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInput.setText(text)

        binding.addRowAtTopButton.setOnClickListener {
            onStartClick.invoke()
            dismiss()
        }
        binding.addRowAtBottomButton.setOnClickListener {
            onEndClick.invoke()
            dismiss()
        }

        binding.textLayout.setStartIconOnClickListener {
            binding.textInput.setText(text)

        }
        binding.confirmButton.setOnClickListener {
            returnChange.invoke(binding.textInput.text.toString())
            dismiss()
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }
}