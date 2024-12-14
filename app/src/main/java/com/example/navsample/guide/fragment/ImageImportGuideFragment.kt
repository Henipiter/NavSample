package com.example.navsample.guide.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.canhub.cropper.CropImageView
import com.example.navsample.R
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class ImageImportGuideFragment : Fragment(), Guide {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()
        val dialog = configureDialog()
        dialog.show(childFragmentManager, "TAG")


    }

    override fun prepare() {
        binding.receiptImage.setImageBitmap(null)
        binding.receiptImage.visibility = View.GONE


        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(null)
            },
            {
                loadCropImageView("original_receipt.jpg")
            },
            {
                loadCropImageView("original_receipt.jpg")
                val selectedArea = Rect(0, 100, 2464, 2467)
                binding.receiptImage.cropRect = selectedArea
            },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_imageImportGuideFragment_to_addReceiptGuideFragment)
            }
        )
        texts = listOf(
            "Load image",
            "Image loaded",
            "Image cropping",
            ""
        )
        verticalLevel = listOf(
            500, 500, 500, 500
        )
    }

    override fun loadImage(imageName: String) {
    }

    override fun loadCropImageView(imageName: String) {
        loadCropImageView(imageName, requireContext())
    }


    override fun getPhotoView(): PhotoView {
        TODO()
    }

    override fun getCropImageView(): CropImageView {
        return binding.receiptImage
    }


}
