package com.example.navsample.fragments

import android.graphics.ImageDecoder
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
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class ImageImportFragment : Fragment() {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            receiptImageViewModel.uri.value = uri
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            receiptImageViewModel.bitmapOriginal.value = bitmap
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
        binding.toolbar.visibility = View.GONE
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
        imageAnalyzerViewModel.uid = receiptImageViewModel.uid.value ?: "temp"
        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.rotate -> {
                    Toast.makeText(requireContext(), "ROTATE", Toast.LENGTH_SHORT).show()
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
                receiptImageViewModel.bitmapCroppedReceipt.value = bitmap
                val analyzedImage = InputImage.fromBitmap(bitmap, 0)
                imageAnalyzerViewModel.analyzeReceipt(analyzedImage)
            }
        }

        binding.manualButton.setOnClickListener {
            receiptDataViewModel.store = MutableLiveData<Store>(null)
            receiptDataViewModel.receipt = MutableLiveData<Receipt>(null)
            receiptImageViewModel.clearData()
            Navigation.findNavController(requireView())
                .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
        }

        binding.analyzeButton.setOnClickListener {
            binding.receiptImage.croppedImageAsync()
            Navigation.findNavController(requireView())
                .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)

        }

    }


    private fun initObserver() {
        receiptImageViewModel.bitmapOriginal.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(it)
            } else {
                binding.receiptImage.visibility = View.GONE
                binding.toolbar.visibility = View.GONE
            }
        }

    }

}
