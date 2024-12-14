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
import com.example.navsample.databinding.FragmentCropImageBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView

class CropProductGuideFragment : Fragment(), Guide {

    private var _binding: FragmentCropImageBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()

        binding.toolbar.inflateMenu(R.menu.top_menu_crop)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.title = "Crop only products section"


        val dialog = configureDialog()
        dialog.show(childFragmentManager, "TAG")


    }

    override fun prepare() {
        binding.receiptImage.visibility = View.VISIBLE

        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadCropImageView("crop_receipt.png") },
            {
                loadCropImageView("crop_receipt.png")
                val selectedArea = Rect(0, 1120, 2464, 1450)
                binding.receiptImage.cropRect = selectedArea
            },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_cropProductGuideFragment_to_addProductListGuideFragment)
            }
        )


        texts = listOf(
            "Cut to receipt",
            "Crop to product",
            ""
        )
        verticalLevel = listOf(
            100, 100, 100, 100
        )
    }

    override fun getPhotoView(): PhotoView {
        TODO("Not yet implemented")
    }

    override fun loadImage(imageName: String) {

    }

    override fun loadCropImageView(imageName: String) {
        loadCropImageView(imageName, requireContext())
    }


    override fun getCropImageView(): CropImageView {
        return binding.receiptImage
    }
}