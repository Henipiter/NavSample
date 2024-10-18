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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.dto.Utils.Companion.roundDouble
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel


@ExperimentalGetImage
class AddProductListFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var myPref: SharedPreferences
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
    private var onStart = true
    private var isPricesSumValid = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductListBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        receiptImageViewModel.bitmapCroppedProduct.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(receiptImageViewModel.bitmapCroppedProduct.value)
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = true
            } else {
                binding.receiptImage.visibility = View.GONE
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
            }
        }
        receiptDataViewModel.product.observe(viewLifecycleOwner) {
            it?.let {
                productListAdapter.productList = it
                productListAdapter.notifyDataSetChanged()
                recalculateSumOfPrices()
            }
        }

        receiptDataViewModel.receipt.observe(viewLifecycleOwner) {
            binding.receiptValueText.text = receiptDataViewModel.receipt.value?.pln.toString()
        }

        receiptDataViewModel.reorderedProductTiles.observe(viewLifecycleOwner) {
            if (myPref.getBoolean(OPEN_REORDER_FRAGMENT, false) && it == true) {
                receiptDataViewModel.reorderedProductTiles.value = false
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
            Toast.makeText(requireContext(), "'$it'", Toast.LENGTH_SHORT).show()
            binding.geminiResponse.text = it
        }

        imageAnalyzerViewModel.productAnalyzed.observe(viewLifecycleOwner) { it ->
            if (it == null) {
                return@observe
            }
            receiptDataViewModel.product.value = it.productList
            receiptDataViewModel.algorithmOrderedNames.value =
                it.receiptNameLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>
            receiptDataViewModel.algorithmOrderedPrices.value =
                it.receiptPriceLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>

            receiptDataViewModel.reorderedProductTiles.value = true

        }
    }

    private fun recalculateSumOfPrices() {
        var sum = 0.0
        productListAdapter.productList.forEach {
            sum += it.finalPrice
        }
        sum = roundDouble(sum)
        binding.cartValueText.text = sum.toString()

        val final = receiptDataViewModel.receipt.value?.pln
        if (final != sum) {
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
        binding.countText.text = productListAdapter.productList.size.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false

        receiptDataViewModel.refreshProductListForReceipt(id)
        myPref =
            requireContext().getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)

        if (onStart && receiptImageViewModel.uriCroppedProduct.value == null) {
            onStart = false
            Navigation.findNavController(requireView())
                .navigate(R.id.action_addProductListFragment_to_cropImageFragment)
        }
        initObserver()
        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            receiptDataViewModel.product.value ?: arrayListOf(),
            receiptDataViewModel.categoryList.value ?: arrayListOf(),
            this
        ) { i: Int ->
            receiptDataViewModel.product.value?.get(i)?.let {
                ConfirmDialog(
                    "Delete",
                    "Are you sure you want to delete the product??\n\n" + "Name: " + it.name + "\nPLN: " + it.subtotalPrice
                ) {
                    if (it.id != null && it.id!! >= 0) {
                        receiptDataViewModel.deleteProduct(it.id!!)
                    }
                    receiptDataViewModel.product.value?.removeAt(i)
                    productListAdapter.productList =
                        receiptDataViewModel.product.value ?: arrayListOf()
                    productListAdapter.notifyDataSetChanged()
                    recalculateSumOfPrices()
                }.show(childFragmentManager, "TAG")
            }
        }


        binding.receiptImage.setOnLongClickListener {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_addProductListFragment_to_cropImageFragment)
            true
        }
        binding.toolbar.title = receiptDataViewModel.store.value?.name
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reorder -> {
                    imageAnalyzerViewModel.productAnalyzed.value = null
                    reorderTilesWithProducts()
                    true
                }

                R.id.add_new -> {
                    val action =
                        AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                            false, -1
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
                        { save() }.show(childFragmentManager, "TAG")
                    } else {
                        save()
                    }
                    true
                }

                else -> false
            }
        }

    }

    private fun save() {
        if (!isPricesSumValid) {
            receiptDataViewModel.receipt.value?.let {
                it.validPrice = false

                receiptDataViewModel.updateReceipt(it)
            }
        }
        receiptDataViewModel.insertProducts(
            receiptDataViewModel.product.value?.toList() ?: listOf()
        )
        listingViewModel.loadDataByProductFilter()
        imageAnalyzerViewModel.clearData()
        Navigation.findNavController(binding.root).popBackStack(R.id.settingsFragment, false)
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
        Navigation.findNavController(requireView())
            .navigate(R.id.action_addProductListFragment_to_experimentRecycleFragment)

    }

    override fun onItemClick(index: Int) {
        val action =
            AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                false, index
            )
        Navigation.findNavController(requireView()).navigate(action)

    }

    companion object {

        private const val OPEN_REORDER_FRAGMENT = "openReorderFragment"
    }

}
