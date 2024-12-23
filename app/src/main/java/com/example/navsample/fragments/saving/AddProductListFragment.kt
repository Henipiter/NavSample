package com.example.navsample.fragments.saving


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.analyzer.AnalyzedProductsData
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.entities.Product
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.viewmodels.ExperimentalDataViewModel
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel


@ExperimentalGetImage
class AddProductListFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddProductListFragmentArgs by navArgs()

    private lateinit var myPref: SharedPreferences
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val experimentalDataViewModel: ExperimentalDataViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val reorderedProductTiles = MutableLiveData(false)
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
    private var shouldOpenCropFragment = true
    private var firstEntry = true
    private var isPricesSumValid = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductListBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)

        addProductDataViewModel.refreshCategoryList()
        initObserver()
        if (firstEntry) {
            firstEntry = false
            consumeNavArgs()
        }
        myPref =
            requireContext().getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        if (shouldOpenCropFragment && imageViewModel.uriCroppedProduct.value == null) {
            shouldOpenCropFragment = false
            val action =
                AddProductListFragmentDirections.actionAddProductListFragmentToCropImageFragment(
                    receiptId = navArgs.receiptId,
                    categoryId = navArgs.categoryId
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            addProductDataViewModel.productList.value ?: mutableListOf(),
            addProductDataViewModel.categoryList.value ?: listOf(),
            this
        ) { i: Int ->
            addProductDataViewModel.productList.value?.let { productList ->
                val product = productList[i]
                ConfirmDialog(
                    "Delete",
                    "Are you sure you want to delete the product??\n\n" +
                            "Name: ${product.name}\nPLN: ${product.subtotalPrice}"
                ) {
                    if (product.id.isNotEmpty()) {
                        addProductDataViewModel.deleteProduct(product.id)
                    }
                    productList.removeAt(i)
                    productListAdapter.productList = productList
                    productListAdapter.notifyDataSetChanged()
                    calculateSumOfProductPrices(productList)
                }.show(childFragmentManager, "TAG")
            }
        }


        binding.receiptImage.setOnLongClickListener {
            val action =
                AddProductListFragmentDirections.actionAddProductListFragmentToCropImageFragment(
                    receiptId = navArgs.receiptId,
                    categoryId = navArgs.categoryId
                )
            Navigation.findNavController(requireView()).navigate(action)
            true
        }
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.toolbar.setNavigationOnClickListener {
            shouldOpenCropFragment = true
            Navigation.findNavController(it).popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.aiParser -> {
                    val productsData = AnalyzedProductsData(
                        ArrayList(addProductDataViewModel.productList.value?.map { product -> product.name }
                            ?: arrayListOf()),
                        ArrayList(),
                        addProductDataViewModel.productList.value ?: arrayListOf()
                    )

                    imageAnalyzerViewModel.aiAnalyze(
                        productsData, addProductDataViewModel.categoryList.value ?: listOf()
                    )
                    true
                }

                R.id.reorder -> {
                    imageAnalyzerViewModel.productAnalyzed.value = null
                    reorderTilesWithProducts()
                    true
                }

                R.id.add_new -> {
                    val action =
                        AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                            inputType = AddingInputType.EMPTY.name,
                            receiptId = navArgs.receiptId,
                            storeId = navArgs.storeId,
                            sourceFragment = FragmentName.ADD_PRODUCT_LIST_FRAGMENT,
                            productId = "",
                            categoryId = ""
                        )
                    Navigation.findNavController(requireView()).navigate(action)
                    true
                }

                R.id.confirm -> {
                    if (!isProductsAreValid()) {
                        ConfirmDialog(
                            "Invalid prices",
                            "Some products have invalid prices. Continue?"
                        )
                        {
                            save()
                        }.show(childFragmentManager, "TAG")
                    } else {
                        save()
                    }
                    true
                }

                else -> false
            }
        }

    }


    private fun consumeNavArgs() {
        if (navArgs.storeId.isNotEmpty()) {
            addProductDataViewModel.getStoreById(navArgs.storeId)
        } else {
            throw Exception("NO STORE ID SET")
        }
        if (navArgs.receiptId.isNotEmpty()) {
            addProductDataViewModel.getReceiptById(navArgs.receiptId)
            addProductDataViewModel.getProductsByReceiptId(navArgs.receiptId)
        } else {
            throw Exception("NO RECEIPT ID SET")
        }

    }

    private fun calculateSumOfProductPrices(productList: List<Product>) {
        var sum = 0
        productList.forEach {
            sum += it.finalPrice
        }
        val textSum = intPriceToString(sum)
        binding.cartValueText.text = textSum


        val receiptPrice = binding.receiptValueText.text
        if (receiptPrice != textSum) {
            isPricesSumValid = false
            binding.cartValueText.setTextColor(Color.RED)
        } else {
            isPricesSumValid = true
            binding.cartValueText.setTextColor(
                resources.getColor(
                    R.color.basic_text_grey, requireContext().theme
                )
            )
        }
        binding.countText.text = productList.size.toString()
    }

    private fun save() {
        if (!isPricesSumValid) {
            addProductDataViewModel.receiptById.value?.let {
                it.validPrice = false
                addProductDataViewModel.updateReceipt(it)
            }
        }
        addProductDataViewModel.insertProducts(
            addProductDataViewModel.productList.value?.toList() ?: listOf()
        )
        imageAnalyzerViewModel.clearData()
        imageViewModel.clearData()
        listingViewModel.loadDataByProductFilter()
        listingViewModel.loadDataByReceiptFilter()
        addProductDataViewModel.cropImageFragmentOnStart = true
        Navigation.findNavController(binding.root).popBackStack(R.id.listingFragment, false)
    }

    private fun isProductsAreValid(): Boolean {
        productListAdapter.productList.forEach { productItem ->
            if (!productItem.validPrice) {
                return false
            }
        }
        return true
    }

    private fun reorderTilesWithProducts() {
        val action =
            AddProductListFragmentDirections.actionAddProductListFragmentToExperimentRecycleFragment(
                receiptId = navArgs.receiptId,
                categoryId = navArgs.categoryId
            )
        Navigation.findNavController(requireView()).navigate(action)

    }

    override fun onItemClick(index: Int) {
        val action =
            AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                inputType = AddingInputType.INDEX.name,
                productIndex = index,
                receiptId = navArgs.receiptId,
                storeId = navArgs.storeId,
                sourceFragment = FragmentName.ADD_PRODUCT_LIST_FRAGMENT,
                productId = "",
                categoryId = ""
            )
        Navigation.findNavController(requireView()).navigate(action)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        imageViewModel.bitmapCroppedProduct.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(imageViewModel.bitmapCroppedProduct.value)
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = true
            } else {
                binding.receiptImage.visibility = View.GONE
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
            }
        }


        reorderedProductTiles.observe(viewLifecycleOwner) {
            if (myPref.getBoolean(OPEN_REORDER_FRAGMENT, false) && it == true) {
                reorderedProductTiles.value = false
                reorderTilesWithProducts()
            }
        }

        imageAnalyzerViewModel.isGeminiWorking.observe(viewLifecycleOwner) {
            if (it) {
                binding.geminiWorkingView.visibility = View.VISIBLE
            } else {
                binding.geminiWorkingView.visibility = View.INVISIBLE
            }
        }

        imageAnalyzerViewModel.geminiResponse.observe(viewLifecycleOwner) {
            it?.let {
                binding.geminiResponse.text = it
                imageAnalyzerViewModel.geminiResponse.value = null
            }
        }
        imageAnalyzerViewModel.geminiError.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), "ERROR: '$it'", Toast.LENGTH_SHORT).show()
                imageAnalyzerViewModel.geminiError.value = null
            }
        }

        imageAnalyzerViewModel.productAnalyzed.observe(viewLifecycleOwner) { it ->
            if (it == null) {
                return@observe
            }
            addProductDataViewModel.productList.value = it.productList
            experimentalDataViewModel.algorithmOrderedNames.value =
                it.receiptNameLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>
            experimentalDataViewModel.algorithmOrderedPrices.value =
                it.receiptPriceLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>

            reorderedProductTiles.value = true
        }

        addProductDataViewModel.storeById.observe(viewLifecycleOwner) { store ->
            store?.let {
                binding.toolbar.title = store.name
            }
        }
        addProductDataViewModel.receiptById.observe(viewLifecycleOwner) { receipt ->
            receipt?.let {
                binding.receiptValueText.text = intPriceToString(receipt.pln)
            }
        }
        addProductDataViewModel.productList.observe(viewLifecycleOwner) { productList ->
            productListAdapter.productList = productList
            calculateSumOfProductPrices(productList)
            if (productListAdapter.categoryList.isNotEmpty()) {
                productListAdapter.notifyDataSetChanged()
            }
        }
        addProductDataViewModel.categoryList.observe(viewLifecycleOwner) { categoryList ->
            productListAdapter.categoryList = categoryList
            if (productListAdapter.productList.isNotEmpty()) {
                productListAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {

        private const val OPEN_REORDER_FRAGMENT = "openReorderFragment"
    }

}
