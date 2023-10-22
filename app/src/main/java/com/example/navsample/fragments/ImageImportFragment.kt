package com.example.navsample.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.ImageAnalyzer
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


class ImageImportFragment : Fragment() {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var analyzedImage: InputImage? = null
    private val imageAnalyzer = ImageAnalyzer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.applyButton.visibility = View.INVISIBLE

        initObserver()



        binding.loadImage.setOnClickListener {
            val action = ImageImportFragmentDirections.actionImageImportFragmentToCropFragment()
            Navigation.findNavController(view).navigate(action)
            binding.applyButton.visibility = View.INVISIBLE
        }

        binding.manualButton.setOnClickListener {
            val action =
                ImageImportFragmentDirections.actionImageImportFragmentToStageBasicInfoFragment()
            Navigation.findNavController(it).navigate(action)
        }

        binding.analyzeButton.setOnClickListener {
            analyzedImage?.let { it1 ->
                imageAnalyzer.analyzeReceipt(
                    it1
                ) {
                    drawRectangles()

                    receiptDataViewModel.receipt.value = imageAnalyzer.receipt
                    binding.applyButton.visibility = View.VISIBLE
                }
            }
        }

        binding.applyButton.setOnClickListener {

            val action =
                ImageImportFragmentDirections.actionImageImportFragmentToStageBasicInfoFragment()
            Navigation.findNavController(requireView()).navigate(action)

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
                val action =
                    ImageImportFragmentDirections.actionImageImportFragmentToStageBasicInfoFragment()
                Navigation.findNavController(requireView()).navigate(action)
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

    private fun drawRectangle(pixel: ImageAnalyzer.Pixel?, canvas: Canvas, paint: Paint) {
        if (pixel != null) {
            drawLine(pixel.x2, pixel.y2, pixel.x1, pixel.y2, canvas, paint)
        }
    }

    private fun drawLine(
        startX: Int,
        startY: Int,
        stopX: Int,
        stopY: Int,
        canvas: Canvas,
        paint: Paint
    ) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
    }

}
