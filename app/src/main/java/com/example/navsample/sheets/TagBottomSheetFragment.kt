package com.example.navsample.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navsample.databinding.BottomSheetTagBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TagBottomSheetFragment(
    private var onDelete: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTagBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteButton.setOnClickListener {
            onDelete.invoke()
            dismiss()
        }
    }

    companion object {
        const val TAG = "TagBottomSheetFragment"
    }
}