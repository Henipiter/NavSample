package com.example.navsample.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.ImageAnalyzer
import com.example.navsample.R
import com.example.navsample.databinding.FragmentImageImportBinding
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class ImageImportFragment : Fragment() {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    private val args: ImageImportFragmentArgs by navArgs()


    private var analyzedImage: InputImage? = null
    private val imageAnalyzer = ImageAnalyzer()
    private var analyzedBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (args.bitmap != null) {
            binding.receiptImageBig.setImageBitmap(args.bitmap)
        }

        binding.cameraButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_imageImportFragment_to_cameraFragment)
        }
        binding.manualButton.setOnClickListener {
            val action = ImageImportFragmentDirections.actionImageImportFragmentToStageBasicInfoFragment(
                arrayOf()
            )
            Navigation.findNavController(it).navigate(action)
        }
        binding.applyButton.setOnClickListener {
            val uri = drawRectangles()
            val action = ImageImportFragmentDirections.actionImageImportFragmentToStageBasicInfoFragment(
                imageAnalyzer.productList,
                uri,
                imageAnalyzer.receipt
            )
            Navigation.findNavController(requireView()).navigate(action)
        }

        val pickPhoto = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                analyzedBitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                analyzedImage = InputImage.fromFilePath(requireContext(), it)
                binding.receiptImageBig.setImageBitmap(analyzedBitmap)
                analyzedImage?.let { it1 -> imageAnalyzer.processImageProxy(it1, requireContext()) }
            }
        }
        binding.storageButton.setOnClickListener {
            pickPhoto.launch("image/*")
        }
    }

    private fun drawRectangles(): Uri {

        if (analyzedBitmap == null) {
            return getImageUri(Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888))
        }
        val analyzedBitmap = analyzedBitmap!!
        val markedBitmap = analyzedBitmap.copy(analyzedBitmap.config, true)

        val canvas = Canvas(markedBitmap)
        val paint = Paint(Color.GREEN)
        paint.strokeWidth = 10F
        drawRectangle(imageAnalyzer.pixelNIP, canvas, paint)
        drawRectangle(imageAnalyzer.pixelDate, canvas, paint)
        drawRectangle(imageAnalyzer.pixelTime, canvas, paint)
        return getImageUri(markedBitmap)
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

    private fun getImageUri(inImage: Bitmap): Uri {

        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }
}