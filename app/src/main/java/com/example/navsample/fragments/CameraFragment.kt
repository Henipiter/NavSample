package com.example.navsample

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.databinding.FragmentCameraBinding
import com.example.navsample.viewmodels.ImageViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val imageViewModel: ImageViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.bottomText.setOnClickListener {
            takePhoto()
        }
    }

    private fun fixImageRotation(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("DADADA", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.d("DADADA", "Rotation Degrees: ${image.imageInfo.rotationDegrees}")
                    Log.d(
                        "DADADA",
                        "Screen Rotation: ${requireActivity().windowManager.defaultDisplay.rotation}"
                    )


                    Log.d("DADADA", "${image.imageInfo.rotationDegrees}")
                    imageViewModel.bitmapOriginal.postValue(fixImageRotation(image))
                    image.close()
                    Navigation.findNavController(requireView()).popBackStack()
                }

            }
        )
    }

    override fun onResume() {
        super.onResume()
        imageCapture?.targetRotation = requireActivity().windowManager.defaultDisplay.rotation
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }

            if (activity == null) {
                Toast.makeText(requireContext(), "Activity null", Toast.LENGTH_SHORT).show()
            } else {
                val rotation = requireActivity().windowManager.defaultDisplay.rotation
                imageCapture = ImageCapture.Builder().setTargetRotation(rotation).build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_SHORT)
                    .show()
            } else {
                startCamera()
            }
        }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXApp"

        private val REQUIRED_PERMISSIONS =
            mutableListOf(android.Manifest.permission.CAMERA).toTypedArray()
    }

}
