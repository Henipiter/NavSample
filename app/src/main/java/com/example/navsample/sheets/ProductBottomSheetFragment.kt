package com.example.navsample.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navsample.databinding.BottomSheetProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductBottomSheetFragment(
    private var onDelete: () -> Unit,
    private var onJumpToStore: () -> Unit,
    private var onJumpToCategory: () -> Unit,
    private var onJumpToReceipt: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProductBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteButton.setOnClickListener {
            onDelete.invoke()
            dismiss()
        }
        binding.jumpToStoreButton.setOnClickListener {
            onJumpToStore.invoke()
            dismiss()
        }
        binding.jumpToCategoryButton.setOnClickListener {
            onJumpToCategory.invoke()
            dismiss()
        }
        binding.jumpToReceiptButton.setOnClickListener {
            onJumpToReceipt.invoke()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ProductBottomSheetFragment"
    }
}