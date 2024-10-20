package com.example.navsample.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogConfirmBinding

class ConfirmDialog(
    private var title: String,
    private var description: String,
    private var onConfirmClick: () -> Unit,
) : DialogFragment() {
    private var _binding: DialogConfirmBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = DialogConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = title
        binding.descriptionText.text = description


        binding.confirmButton.setOnClickListener {
            onConfirmClick.invoke()
            dismiss()
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }
}