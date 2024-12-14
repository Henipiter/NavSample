package com.example.navsample.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navsample.databinding.BottomSheetReceiptBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReceiptBottomSheetFragment(
    private var onDelete: () -> Unit,
    private var onJumpToStore: () -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetReceiptBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteButton.setOnClickListener {
            onDelete.invoke()
        }
        binding.jumpToStoreButton.setOnClickListener {
            onJumpToStore.invoke()
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}