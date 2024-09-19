package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class ExperimentalListGuideFragment : Fragment(), Guide {
    private var _binding: FragmentExperimentRecycleBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        configureDialog().show(childFragmentManager, "TAG")
    }

    override fun prepare() {
        loadImage("short_crop_receipt.png")
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage("short_crop_receipt.png") },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_experimentalListGuideFragment_to_addProductListGuideFragment)
            }
        )
        texts = listOf(
            "Text",
            ""
        )
        verticalLevel = listOf(
            100, 100, 100
        )
    }


    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }

    override fun getPhotoView(): PhotoView {
        return binding.receiptImageBig
    }
}
