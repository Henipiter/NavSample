package com.example.navsample.fragments.saving


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.navsample.sheets.ImportImageBottomSheetFragment
import com.example.navsample.sheets.ReceiptBottomSheetFragment
import com.example.navsample.viewmodels.ExperimentalDataViewModel
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel
import kotlinx.coroutines.runBlocking


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
            delegateToCropImage()
        }
        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            addProductDataViewModel.aggregatedProductList.value ?: mutableListOf(),
            addProductDataViewModel.categoryList.value ?: listOf(),
            this
        ) { index: Int ->
            ConfirmDialog(
                getString(R.string.delete_confirmation_title),
                getString(R.string.delete_product_confirmation_dialog)
            ) {
                deleteFromLists(index)
            }.show(childFragmentManager, "TAG")

        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )

        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.toolbar.setNavigationOnClickListener {
            shouldOpenCropFragment = true
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }
        defineToolbarActions()
    }

    private fun deleteFromLists(index: Int) {
        val databaseSize = addProductDataViewModel.databaseProductList.value?.size ?: 0
        if (index < databaseSize) {
            addProductDataViewModel.databaseProductList.value?.let { productList ->
                addProductDataViewModel.deleteProduct(productList[index].id)
                productList.removeAt(index)
            }
        } else {
            val temporaryListIndex = index - databaseSize
            addProductDataViewModel.temporaryProductList.value?.removeAt(temporaryListIndex)
        }

        addProductDataViewModel.aggregatedProductList.value?.removeAt(index)
        productListAdapter.notifyItemRemoved(index)
        calculateSumOfProductPrices(
            addProductDataViewModel.databaseProductList.value ?: arrayListOf()
        )
    }

    private fun clearInputs() {
        addProductDataViewModel.temporaryProductList.value?.clear()
        addProductDataViewModel.databaseProductList.value?.clear()
        addProductDataViewModel.aggregatedProductList.value?.clear()
        addProductDataViewModel.productById.value = null
    }

    private fun defineToolbarActions() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.importImage -> {
                    importImage()
                    true
                }

                R.id.aiAssistant -> {
                    if (isInternetAvailable()) {
                        val productsData = AnalyzedProductsData(
                            ArrayList(addProductDataViewModel.aggregatedProductList.value?.map { product -> product.name }
                                ?: arrayListOf()),
                            ArrayList(),
                            addProductDataViewModel.temporaryProductList.value ?: arrayListOf(),
                            addProductDataViewModel.databaseProductList.value ?: arrayListOf()
                        )
                        imageAnalyzerViewModel.aiAnalyze(
                            productsData, addProductDataViewModel.categoryList.value ?: listOf()
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.check_internet_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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


    private fun delegateToCamera() {
        shouldOpenCropFragment = true
        val action = AddProductListFragmentDirections.actionAddProductListFragmentToCameraFragment(
            source = FragmentName.ADD_PRODUCT_LIST_FRAGMENT
        )
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun delegateToCropImage(showToast: Boolean = false) {
        if (imageViewModel.bitmapCroppedReceipt.value == null) {
            if (showToast) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_image_set),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        val action =
            AddProductListFragmentDirections.actionAddProductListFragmentToCropImageFragment(
                receiptId = navArgs.receiptId,
                categoryId = navArgs.categoryId
            )
        Navigation.findNavController(requireView()).navigate(action)
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
        runBlocking {
            addProductDataViewModel.insertProducts(
                addProductDataViewModel.aggregatedProductList.value?.toList() ?: listOf()
            )
            listingViewModel.loadDataByProductFilter()
            listingViewModel.loadDataByReceiptFilter()
        }
        imageAnalyzerViewModel.clearData()
        imageViewModel.clearData()
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

    private fun importImage() {
        popUpButtonSheet()
    }

    private fun popUpButtonSheet() {
        val modalBottomSheet = ImportImageBottomSheetFragment(
            onCameraCapture = { delegateToCamera() },
            onBrowseGallery = {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCrop = { delegateToCropImage(true) },
            visibleOnCrop = imageViewModel.bitmapCroppedReceipt.value != null
        )
        modalBottomSheet.show(parentFragmentManager, ReceiptBottomSheetFragment.TAG)
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
                reorderedProductTiles.postValue(false)
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
            it?.let { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                imageAnalyzerViewModel.geminiError.value = null
            }
        }

        imageAnalyzerViewModel.productAnalyzed.observe(viewLifecycleOwner) { it ->
            if (it == null) {
                return@observe
            }
            addProductDataViewModel.temporaryProductList.postValue(ArrayList(it.temporaryProductList))
            addProductDataViewModel.databaseProductList.postValue(ArrayList(it.databaseProductList))
            experimentalDataViewModel.algorithmOrderedNames.postValue(
                ArrayList(it.receiptNameLines.map { AlgorithmItemAdapterArgument(it) })
            )
            experimentalDataViewModel.algorithmOrderedPrices.postValue(
                ArrayList(it.receiptPriceLines.map { AlgorithmItemAdapterArgument(it) })
            )

            reorderedProductTiles.postValue(true)
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
        addProductDataViewModel.databaseProductList.observe(viewLifecycleOwner) {
            addProductDataViewModel.aggregateProductList()
        }
        addProductDataViewModel.temporaryProductList.observe(viewLifecycleOwner) {
            addProductDataViewModel.aggregateProductList()
        }
        addProductDataViewModel.aggregatedProductList.observe(viewLifecycleOwner) { productList ->
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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {

                imageViewModel.uri.value = uri
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                imageViewModel.bitmapCroppedReceipt.value = bitmap
                binding.receiptImage.setImageBitmap(bitmap)

                binding.receiptImage.visibility = View.VISIBLE
                delegateToCropImage()
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    companion object {

        private const val OPEN_REORDER_FRAGMENT = "openReorderFragment"
    }

}
