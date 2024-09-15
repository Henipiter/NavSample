package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentGuideScreenshotBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView

class ScreenshotGuideFragment : Fragment(), Guide {

    private var _binding: FragmentGuideScreenshotBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>

    private val imagesToShow = arrayListOf(
        "original_receipt_cropping.jpg",
        "original_receipt_cropping_2.jpg",
        "crop_receipt_cropping.jpg"
    )

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


    override fun prepare() {
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage(imagesToShow[0]) },
            { loadImage(imagesToShow[1]) },
            { loadImage(imagesToShow[2]) },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_screenshotGuideFragment_to_addReceiptGuideFragment)
            }
        )
        texts = listOf(
            "Have to cut",
            "Cut to receipt",
            "Cut to products",
            ""
        )
    }

    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }

    override fun getPhotoView(): PhotoView {
        return binding.receiptImageBig
    }
}
