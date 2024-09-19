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
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        val dialog = configureDialog()

        dialog.show(childFragmentManager, "TAG")
    }

    override fun prepare() {
        binding.indeterminateBar.visibility = View.GONE
        binding.receiptImageBig.setImageBitmap(null)

        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { binding.receiptImageBig.setImageBitmap(null) },
            { loadImage("original_receipt.jpg") },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_imageImportGuideFragment_to_screenshotGuideFragment)
            }
        )
        texts = listOf(
            "Load image",
            "Image loaded",
            ""
        )
        verticalLevel = listOf(
            100, 200, 300
        )
    }

    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }


    override fun getPhotoView(): PhotoView {
        return binding.receiptImageBig
    }
}
