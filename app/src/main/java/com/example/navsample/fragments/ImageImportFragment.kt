package com.example.navsample.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.imageanalyzer.ImageAnalyzer
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class ImageImportFragment : Fragment() {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var analyzedImage: InputImage? = null
    private lateinit var imageAnalyzer: ImageAnalyzer
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
        binding.indeterminateBar.visibility = View.GONE

        initObserver()

        imageAnalyzer = ImageAnalyzer()
        imageAnalyzer.uid = receiptImageViewModel.uid.value ?: "temp"
        binding.loadImage.setOnClickListener {
            val action = ImageImportFragmentDirections.actionImageImportFragmentToCropFragment()
            Navigation.findNavController(view).navigate(action)
        }
        binding.receiptImageBig.setOnLongClickListener {
            startCameraWithUri()
            true
        }
        binding.manualButton.setOnClickListener {
            val action =
                ImageImportFragmentDirections.actionImageImportFragmentToAddReceiptFragment()
            Navigation.findNavController(it).navigate(action)
        }

        binding.analyzeButton.setOnClickListener {
            goNext = true
            binding.indeterminateBar.visibility = View.VISIBLE

            analyzedImage?.let { it1 ->
                imageAnalyzer.analyzeReceipt(
                    it1
                ) {
//                    drawRectangles()

                    val store = Store(imageAnalyzer.valueNIP, imageAnalyzer.companyName, 0)
                    val receipt = Receipt(
                        -1,
                        imageAnalyzer.valuePLN.toString().toFloat(),
                        imageAnalyzer.valuePTU.toString().toFloat(),
                        imageAnalyzer.valueDate,
                        imageAnalyzer.valueTime
                    )
                    Log.i("ImageProcess", "valueNIP ${imageAnalyzer.valueNIP}")
                    Log.i("ImageProcess", "companyName ${imageAnalyzer.companyName}")
                    Log.i("ImageProcess", "valuePTU ${imageAnalyzer.valuePTU}")
                    Log.i("ImageProcess", "valuePLN ${imageAnalyzer.valuePLN}")
                    Log.i("ImageProcess", "valueDate ${imageAnalyzer.valueDate}")
                    Log.i("ImageProcess", "valueTime ${imageAnalyzer.valueTime}")
                    receiptDataViewModel.store.value = store
                    receiptDataViewModel.receipt.value = receipt
                }
            }
        }

    }

    private fun initObserver() {
        receiptImageViewModel.bitmap.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmap.value != null) {
                    binding.receiptImageBig.setImageBitmap(receiptImageViewModel.bitmap.value)
                    analyzedImage = InputImage.fromBitmap(receiptImageViewModel.bitmap.value!!, 0)
                }
            }
        }
        receiptDataViewModel.receipt.observe(viewLifecycleOwner) {
            it?.let {

                binding.indeterminateBar.visibility = View.GONE
                if (goNext == true) {
                    goNext = false
                    val action =
                        ImageImportFragmentDirections.actionImageImportFragmentToAddReceiptFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                }
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
}
