package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.canhub.cropper.CropImageView
import com.example.navsample.databinding.FragmentGuideScreenshotBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView

open class ScreenshotGuideFragment : Fragment(), Guide {

    private var _binding: FragmentGuideScreenshotBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideScreenshotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        configureDialog().show(childFragmentManager, "TAG")
    }


    override fun prepare() {}

    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }

    override fun loadCropImageView(imageName: String) {
        TODO("Not yet implemented")
    }

    override fun getPhotoView(): PhotoView {
        return binding.receiptImage
    }

    override fun getCropImageView(): CropImageView {
        TODO("Not yet implemented")
    }
}
