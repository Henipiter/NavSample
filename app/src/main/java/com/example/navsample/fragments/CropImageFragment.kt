package com.example.navsample.fragments

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.databinding.FragmentCropImageBinding
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel


class CropImageFragment : Fragment() {

    private var _binding: FragmentCropImageBinding? = null
    private val binding get() = _binding!!
    private val navArgs: CropImageFragmentArgs by navArgs()
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

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

        imageViewModel.bitmapCroppedReceipt.value.let { bitmap ->
            if (bitmap != null) {
                binding.receiptImage.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "NO IMAGE LOADED", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireView()).popBackStack()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(requireView()).popBackStack(R.id.addReceiptFragment, false)
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.rotate -> {
                    imageViewModel.bitmapCroppedReceipt.value?.let { bitmap ->
                        val rotatedBitmap = rotateBitmap(bitmap)
                        imageViewModel.bitmapCroppedReceipt.value = rotatedBitmap
                        binding.receiptImage.setImageBitmap(rotatedBitmap)
                    }
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
                imageViewModel.bitmapCroppedProduct.value = bitmap
                analyzeImage(bitmap)
                Navigation.findNavController(requireView()).popBackStack()
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        imageAnalyzerViewModel.uid = imageViewModel.uid.value ?: "temp"
        navArgs.receiptId.ifEmpty {
            Toast.makeText(requireContext(), "NO RECEIPT ID", Toast.LENGTH_SHORT).show()
            return
        }
        navArgs.categoryId.ifEmpty {
            Toast.makeText(requireContext(), "NO CATEGORY ID", Toast.LENGTH_SHORT).show()
            return
        }
        imageAnalyzerViewModel.analyzeProductList(
            bitmap,
            navArgs.receiptId,
            navArgs.categoryId,
            listingViewModel.categoryList.value
        )


    }

    private fun rotateBitmap(original: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preRotate(90F)
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }
}