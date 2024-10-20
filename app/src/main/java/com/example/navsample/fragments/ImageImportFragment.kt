package com.example.navsample.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class ImageImportFragment : Fragment() {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            imageViewModel.uri.value = uri
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            imageViewModel.bitmapOriginal.value = bitmap
            binding.receiptImage.setImageBitmap(bitmap)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        binding.toolbar.inflateMenu(R.menu.top_menu_crop)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
        binding.toolbar.menu.findItem(R.id.rotate).isVisible = false
        imageAnalyzerViewModel.uid = imageViewModel.uid.value ?: "temp"
        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.rotate -> {
                    imageViewModel.bitmapOriginal.value?.let {
                        imageViewModel.bitmapOriginal.value = rotateBitmap(it)
                    }
                    Toast.makeText(requireContext(), "ROTATE", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.confirm -> {
                    binding.receiptImage.croppedImageAsync()
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
                    true
                }

                else -> false
            }
        }
        binding.loadImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        binding.receiptImage.setOnCropImageCompleteListener { _, result ->
            result.getBitmap(requireContext())?.let { bitmap ->
                imageViewModel.bitmapCroppedReceipt.value = bitmap
                val analyzedImage = InputImage.fromBitmap(bitmap, 0)
                imageAnalyzerViewModel.analyzeReceipt(analyzedImage)
            }
        }

        binding.manualButton.setOnClickListener {
            imageViewModel.clearData()
            Navigation.findNavController(requireView())
                .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
        }

    }


    private fun initObserver() {
        imageViewModel.bitmapOriginal.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
                binding.toolbar.menu.findItem(R.id.rotate).isVisible = true
                binding.receiptImage.setImageBitmap(it)
            } else {
                binding.receiptImage.visibility = View.GONE
                binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
                binding.toolbar.menu.findItem(R.id.rotate).isVisible = false
            }
        }

    }

    private fun rotateBitmap(original: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preRotate(90F)
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }

}
