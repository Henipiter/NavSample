package com.example.navsample.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navsample.databinding.BottomSheetImportImageBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImportImageBottomSheetFragment(
    private var onCrop: () -> Unit,
    private var onBrowseGallery: () -> Unit,
    private var onCameraCapture: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetImportImageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetImportImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cropButton.setOnClickListener {
            onCrop.invoke()
            dismiss()
        }
        binding.browseGalleryButton.setOnClickListener {
            onBrowseGallery.invoke()
            dismiss()
        }
        binding.cameraCaptureButton.setOnClickListener {
            onCameraCapture.invoke()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ProductBottomSheetFragment"
    }
}