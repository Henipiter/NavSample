package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.canhub.cropper.CropImageView
import com.example.navsample.R
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class AddReceiptGuideFragment : Fragment(), Guide {
    private var _binding: FragmentAddReceiptBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        configureDialog().show(childFragmentManager, "TAG")
    }

    override fun prepare() {
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.importImage).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false

        loadImage("crop_receipt.png")
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { fillInputs(); loadImage("crop_receipt.png") },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addReceiptGuideFragment_to_cropProductGuideFragment)
            }
        )
        texts = listOf(
            "View with receipt data. Will be provided automatically",
            ""
        )
        verticalLevel = listOf(
            100, 100
        )
    }


    private fun fillInputs() {
        binding.storeNameInput.setText("CARREFOUR")
        binding.receiptPLNInput.setText("6.79")
        binding.receiptPLNInput.setText("0.09")
        binding.receiptPTUInput.setText("0.09")
        binding.receiptDateInput.setText("2024-03-16")
        binding.receiptTimeInput.setText("18:40")
    }

    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }

    override fun loadCropImageView(imageName: String) {
        TODO("Not yet implemented")
    }

    override fun getPhotoView(): PhotoView {
        return binding.receiptImage
    }

    override fun getCropImageView(): CropImageView {
        TODO("Not yet implemented")
    }
}
