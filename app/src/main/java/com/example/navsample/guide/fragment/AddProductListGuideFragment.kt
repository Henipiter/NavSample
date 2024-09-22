package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class AddProductListGuideFragment : Fragment(), Guide {
    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        configureDialog().show(childFragmentManager, "TAG")
    }

    override fun prepare() {
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.reorder).setVisible(false)

        loadImage("short_crop_receipt.png")
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage("short_crop_receipt.png") },
            { Toast.makeText(requireContext(), "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show() }
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
        return binding.receiptImage
    }
}
