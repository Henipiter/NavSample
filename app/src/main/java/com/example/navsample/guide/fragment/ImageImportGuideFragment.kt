package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentImageImportBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class ImageImportGuideFragment : Fragment(), Guide {

    private var _binding: FragmentImageImportBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        doStuff()
        configureDialog().show(childFragmentManager, "TAG")
    }

    override fun prepare() {
        binding.indeterminateBar.visibility = View.GONE

        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { binding.receiptImageBig.setImageBitmap(null) },
            { loadImage("original_receipt.jpg") },
            {
                this.iterator = 1
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_imageImportGuideFragment_to_screenshotGuideFragment)
            }
        )
    }

    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }


    override fun getPhotoView(): PhotoView {
        return binding.receiptImageBig
    }
}
