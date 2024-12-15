package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImageView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.guide.Guide
import com.github.chrisbanes.photoview.PhotoView


class AddProductListGuideFragment : Fragment(), Guide, ItemClickListener {
    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
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

    private fun prepareProductList() {
        val productList = arrayListOf(
            Product("0", "B_IMPOR LUZ*", "1", 50, 2489, 124, 0, 124, "B", "", true),
            Product("1", "D_KAJZERKA 50 G", "1", 15000, 37, 555, 0, 555, "D", "", true)
        )

        val category = Category("JEDZENIE", "#33FF57")
        category.id = "1"

        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            productList,
            listOf(category),
            this
        ) { }
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        productListAdapter.notifyDataSetChanged()
    }

    override fun prepare() {
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.importImage).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false

        prepareProductList()

        loadImage("short_crop_receipt.png")
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage("short_crop_receipt.png") },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addProductListGuideFragment_to_experimentalListGuideFragment)
            }
        )
        texts = listOf(
            "Products placed",
            "If there are any errors you can edit them"
        )
        verticalLevel = listOf(
            100, 100, 100
        )
    }


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

    override fun onItemClick(index: Int) {
        TODO("Not yet implemented")
    }
}
