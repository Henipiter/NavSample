package com.example.navsample

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.navsample.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraController: LifecycleCameraController
    private var capturedImage: ImageProxy? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!hasPermissions(requireContext())) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startCamera()
        }

        binding.bottomText.setOnClickListener {
            takePhoto()
            var bitmapVal: Bitmap? = null
            if (capturedImage != null) {
                bitmapVal = imageToBitMap(capturedImage!!)
            }
            val action = CameraFragmentDirections.actionCameraFragmentToShopListFragment(bitmapVal)
            Navigation.findNavController(it).navigate(action)

        }

//        capturedImage


    }

    private fun imageToBitMap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer[bytes]
        val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        imageProxy.close()
        return bitmapImage
    }

    private fun takePhoto() {
        cameraController.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    capturedImage = image

                }

                override fun onError(exception: ImageCaptureException) {
                    exception.message?.let { Log.e("CAMERA", it) }
                    Log.e("CAMERA", exception.stackTraceToString())
                    Toast.makeText(
                        context,
                        "Photo not captured:" + exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
    }

    private fun startCamera() {
        val previewView: PreviewView = binding.cameraView
        cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        previewView.controller = cameraController
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                startCamera()
            }
        }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(android.Manifest.permission.CAMERA).apply { }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }


}