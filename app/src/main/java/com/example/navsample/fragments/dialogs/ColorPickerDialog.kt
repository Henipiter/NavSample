package com.example.navsample.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogColorPickerBinding


class ColorPickerDialog(
    private var color: Int,
    private var onConfirmClick: (Int) -> Unit,
) : DialogFragment() {

    private var _binding: DialogColorPickerBinding? = null

    private val binding get() = _binding!!
    private var pickerColor: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = DialogColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.colorPicker.setInitialColor(color);

        binding.colorPicker.subscribe { color, fromUser, shouldPropagate ->
            pickerColor = color
            binding.colorPreview.setBackgroundColor(color)
        };


        binding.confirmButton.setOnClickListener {
            onConfirmClick.invoke(pickerColor)
            dismiss()
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }
}