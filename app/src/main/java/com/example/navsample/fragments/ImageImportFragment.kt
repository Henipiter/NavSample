package com.example.navsample.fragments

import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
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

    private var goNext = false
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            receiptImageViewModel.uri.value = uri
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            receiptImageViewModel.bitmap.value = bitmap
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

        imageAnalyzerViewModel.uid = receiptImageViewModel.uid.value ?: "temp"
        binding.captureImage.setOnClickListener {

        }
        binding.loadImage.setOnClickListener {
            goNext = true
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        binding.receiptImage.setOnCropImageCompleteListener { listener, cropResult ->
            receiptImageViewModel.bitmap.value = cropResult.bitmap
        }

        binding.manualButton.setOnClickListener {
            receiptDataViewModel.store = MutableLiveData<Store>(null)
            receiptDataViewModel.receipt = MutableLiveData<Receipt>(null)
            receiptImageViewModel.clearData()
            Navigation.findNavController(requireView())
                .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
        }

        binding.receiptImage.setOnCropImageCompleteListener { listener, result ->
            Toast.makeText(requireContext(), "'${result.bitmap?.width ?: 0}'", Toast.LENGTH_SHORT)
                .show()
        }

        binding.analyzeButton.setOnClickListener {
            goNext = true
            receiptImageViewModel.bitmap.value = binding.receiptImage.getCroppedImage()
            receiptImageViewModel.setImageUriOriginal()

            analyzeImage()
        }

    }

    private fun analyzeImage() {
        receiptImageViewModel.bitmap.value?.let { it1 ->
            val analyzedImage = InputImage.fromBitmap(it1, 0)
            imageAnalyzerViewModel.analyzeReceipt(analyzedImage)
        }
    }

    private fun initObserver() {
        receiptImageViewModel.bitmap.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(it)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }


        imageAnalyzerViewModel.receiptAnalyzed.observe(viewLifecycleOwner) {

            if (it == null) {
                return@observe
            }
            val store = Store(it.valueNIP, it.companyName, 0)
            val receipt = Receipt(
                -1,
                it.valuePLN.toString().toDouble(),
                it.valuePTU.toString().toDouble(),
                it.valueDate,
                it.valueTime
            )
            Log.i("ImageProcess", "valueNIP ${it.valueNIP}")
            Log.i("ImageProcess", "companyName ${it.companyName}")
            Log.i("ImageProcess", "valuePTU ${it.valuePTU}")
            Log.i("ImageProcess", "valuePLN ${it.valuePLN}")
            Log.i("ImageProcess", "valueDate ${it.valueDate}")
            Log.i("ImageProcess", "valueTime ${it.valueTime}")
            receiptDataViewModel.store.value = store
            receiptDataViewModel.receipt.value = receipt

            if (goNext) {
                goNext = false
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
            }

        }
    }


    private fun drawLine(
        startX: Int,
        startY: Int,
        stopX: Int,
        stopY: Int,
        canvas: Canvas,
        paint: Paint,
    ) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
    }

}
