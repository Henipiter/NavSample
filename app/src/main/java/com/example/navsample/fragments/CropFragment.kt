package com.example.navsample.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.navsample.databinding.FragmentCropBinding

class CropFragment : Fragment() {

    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!

    private var outputUri: Uri? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful -> {
                Log.d("AIC-Sample", "Original bitmap: ${result.originalBitmap}")
                Log.d("AIC-Sample", "Original uri: ${result.originalUri}")
                Log.d("AIC-Sample", "Output bitmap: ${result.bitmap}")
                Log.d("AIC-Sample", "Output uri: ${result.getUriFilePath(requireContext())}")
                handleCropImageResult(result.uriContent)
            }

            result is CropImage.CancelledResult -> showErrorMessage("cropping image was cancelled by the user")
            else -> showErrorMessage("cropping image failed")
        }
    }

    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraPictureButton.setOnClickListener {
            startCameraWithoutUri(includeCamera = true, includeGallery = false)
        }
        binding.storagePictureButton.setOnClickListener {
            startCameraWithoutUri(includeCamera = false, includeGallery = true)
        }
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

    private fun showErrorMessage(message: String) {
        Log.e("AIC-Sample", "Camera error: $message")
        Toast.makeText(activity, "Crop failed: $message", Toast.LENGTH_SHORT).show()
    }

    private fun handleCropImageResult(uri: Uri?) {
        val bitmap = BitmapFactory.decodeStream(uri?.let {
            requireContext().contentResolver.openInputStream(it)
        })
        val action = CropFragmentDirections.actionCropFragmentToImageImportFragment(bitmap)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }
}
