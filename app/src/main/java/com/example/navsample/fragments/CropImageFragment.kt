package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentCropImageBinding
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


class CropImageFragment : Fragment() {

    private var _binding: FragmentCropImageBinding? = null
    private val binding get() = _binding!!

    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCropImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_crop)
        binding.toolbar.setNavigationIcon(R.drawable.back)

        binding.toolbar.title = "Crop only products section"

        receiptImageViewModel.bitmapCroppedReceipt.value.let { bitmap ->
            if (bitmap != null) {
                binding.receiptImage.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "NO IMAGE LOADED", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireView()).popBackStack()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.rotate -> {
                    Toast.makeText(requireContext(), "ROTATE", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.confirm -> {
                    binding.receiptImage.croppedImageAsync()
                    true
                }

                else -> false
            }
        }
        binding.receiptImage.setOnCropImageCompleteListener { _, result ->
            result.getBitmap(requireContext())?.let { bitmap ->
                receiptImageViewModel.bitmapCroppedProduct.value = bitmap
                val analyzedImage = InputImage.fromBitmap(bitmap, 0)
                imageAnalyzerViewModel.analyzeReceipt(analyzedImage)
                analyzeImage()
                Navigation.findNavController(requireView()).popBackStack()
            }

        }


    }

    private fun analyzeImage() {
        val receiptId = receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException()
        val categoryId =
            receiptDataViewModel.store.value?.defaultCategoryId ?: throw NoStoreIdException()

        imageAnalyzerViewModel.uid = receiptImageViewModel.uid.value ?: "temp"
        receiptImageViewModel.bitmapCroppedProduct.value?.let { bitmap ->
            imageAnalyzerViewModel.analyzeProductList(
                bitmap, receiptId, categoryId, receiptDataViewModel.categoryList.value
            )
        }
    }
}