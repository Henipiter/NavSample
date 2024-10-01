package com.example.navsample.fragments

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
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
            if (allPermissionsGranted()) {
                startCameraWithoutUri(includeCamera = true, includeGallery = false)
            } else {
                requestPermissions()
            }
        }
        binding.loadImage.setOnClickListener {
            goNext = true
            startCameraWithoutUri(includeCamera = false, includeGallery = true)
        }
        binding.receiptImage.setOnLongClickListener {
            goNext = true
            startCameraWithUri()
            true
        }
        binding.manualButton.setOnClickListener {
            receiptDataViewModel.store = MutableLiveData<Store>(null)
            receiptDataViewModel.receipt = MutableLiveData<Receipt>(null)
            receiptImageViewModel.clearData()
            Navigation.findNavController(requireView())
                .navigate(R.id.action_imageImportFragment_to_addReceiptFragment)
        }

        binding.analyzeButton.setOnClickListener {
            goNext = true
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
                analyzeImage()
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

    private fun drawRectangles() {

        if (receiptImageViewModel.bitmap.value == null) {
            val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
            receiptImageViewModel.bitmap.value = bitmap
        }
//        val analyzedBitmap = analyzedBitmap!!
//        val markedBitmap = analyzedBitmap.copy(analyzedBitmap.config, true)
//
//            val canvas = Canvas(markedBitmap)
//        val paint = Paint(Color.GREEN)
//        paint.strokeWidth = 10F
//        drawRectangle(imageAnalyzer.pixelNIP, canvas, paint)
//        drawRectangle(imageAnalyzer.pixelDate, canvas, paint)
//        drawRectangle(imageAnalyzer.pixelTime, canvas, paint)
//        viewModel.bitmap.value = markedBitmap
        receiptImageViewModel.setImageUriOriginal()
    }

//    private fun drawRectangle(pixel: ImageAnalyzerNew.Pixel?, canvas: Canvas, paint: Paint) {
//        if (pixel != null) {
//            drawLine(pixel.x2, pixel.y2, pixel.x1, pixel.y2, canvas, paint)
//        }
//    }

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

    private fun startCameraWithUri() {
        customCropImage.launch(
            CropImageContractOptions(
                uri = receiptImageViewModel.uri.value,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = false,
                ),
            ),
        )
    }

    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent)
        }
    }

    @ExperimentalGetImage
    private fun handleCropImageResult(uri: Uri?) {
        val bitmap = BitmapFactory.decodeStream(uri?.let {
            requireContext().contentResolver.openInputStream(it)
        })
        receiptImageViewModel.bitmap.value = bitmap
        receiptImageViewModel.setImageUriOriginal()
    }

    private fun startCameraWithoutUri(includeCamera: Boolean, includeGallery: Boolean) {
        customCropImage.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = includeCamera,
                    imageSourceIncludeGallery = includeGallery,
                ),
            ),
        )
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(
                requireContext(), "Permission request denied", Toast.LENGTH_SHORT
            ).show()
        } else {
            startCameraWithoutUri(includeCamera = true, includeGallery = false)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(android.Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
