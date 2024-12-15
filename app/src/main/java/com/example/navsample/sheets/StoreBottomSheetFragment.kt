package com.example.navsample.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navsample.databinding.BottomSheetStoreBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StoreBottomSheetFragment(
    private var onDelete: () -> Unit,
    private var onJumpToCategory: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStoreBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteButton.setOnClickListener {
            onDelete.invoke()
            dismiss()
        }
        binding.jumpToCategoryButton.setOnClickListener {
            onJumpToCategory.invoke()
            dismiss()
        }
    }

    companion object {
        const val TAG = "StoreBottomSheetFragment"
    }
}