package com.example.navsample.fragments.listing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentProductListBinding
import com.example.navsample.entities.Product
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.fragments.dialogs.PricePickerDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProductListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter

    private var lowerPrice = ""
    private var higherPrice = ""

    private var text = ""
    private var dateFrom = ""
    private var dateTo = ""

    private var calendarDateFrom = Calendar.getInstance()
    private var calendarDateTo = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        binding.priceBetweenInput.setText("-")

        recyclerViewEvent = binding.recyclerViewEventProducts
        productListAdapter = ProductListAdapter(
            requireContext(),
            receiptDataViewModel.productRichList.value ?: arrayListOf(), this

        ) { i ->
            receiptDataViewModel.product.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the product??\n\nName: " + it.name +
                            "\nPLN: " + it.finalPrice
                ) {
                    if (it.id != null && it.id!! >= 0) {
                        receiptDataViewModel.deleteProduct(it.id!!)
                    }
                    receiptDataViewModel.productRichList.value?.removeAt(i)
                    productListAdapter.productList =
                        receiptDataViewModel.productRichList.value ?: arrayListOf()
                    productListAdapter.notifyDataSetChanged()
                }.show(childFragmentManager, "TAG")
            }
        }
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        refreshList()
        receiptDataViewModel.refreshStoreList()
        receiptDataViewModel.refreshCategoryList()

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            this.text = text.toString()
            refreshList()
        }
        binding.priceBetweenLayout.setEndIconOnClickListener {
            showPricePicker()
            refreshList()
        }
        binding.priceBetweenInput.setOnClickListener {
            showPricePicker()
            refreshList()
        }
        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
            refreshList()
        }
        binding.dateBetweenInput.setOnClickListener {
            showDatePicker()
            refreshList()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            text = ""
            refreshList()
            receiptDataViewModel.refreshReceiptList("")
        }
        binding.categoryNameLayout.setStartIconOnClickListener {
            binding.categoryNameInput.setText("")
            text = ""
            refreshList()
            receiptDataViewModel.refreshReceiptList("")
        }
        binding.categoryNameInput.doOnTextChanged { _, _, _, _ ->
            refreshList()
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
            dateTo = ""
            dateFrom = ""
            refreshList()
        }
        binding.priceBetweenLayout.setStartIconOnClickListener {
            binding.priceBetweenInput.setText("-")
            lowerPrice = ""
            higherPrice = ""
            refreshList()
        }
    }

    private fun showPricePicker() {
        val priceBoundaries =
            binding.priceBetweenInput.text.toString().replace("\\s+".toRegex(), "").split("-")
        PricePickerDialog(priceBoundaries[0], priceBoundaries[1]) { lower, higher ->
            lowerPrice = lower
            higherPrice = higher
            binding.priceBetweenInput.setText(lower + " - " + higher)
        }.show(childFragmentManager, "TAG")

    }

    private fun showDatePicker() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    Pair(
                        calendarDateFrom.timeInMillis,
                        calendarDateTo.timeInMillis,
                    )
                )
                .build()
        dateRangePicker.show(childFragmentManager, "Test")

        dateRangePicker.addOnPositiveButtonClickListener {
            calendarDateFrom.timeInMillis = it.first
            calendarDateTo.timeInMillis = it.second
            dateFrom = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendarDateFrom.time)
            dateTo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendarDateTo.time)
            binding.dateBetweenInput.setText("$dateFrom - $dateTo")
        }
    }

    private fun refreshList() {

        val queryStoreName = binding.storeInput.text.toString()
        val queryCategoryName = binding.categoryNameInput.text.toString()

        val queryFromDate = if (dateFrom == "") "0" else dateFrom
        val queryToDate = if (dateTo == "") "9" else dateTo
        val queryLowerPrice = if (lowerPrice == "") 0F else lowerPrice.toFloat()

        if (higherPrice != "") {
            receiptDataViewModel.refreshProductList(
                queryStoreName,
                queryCategoryName,
                queryFromDate,
                queryToDate,
                queryLowerPrice,
                higherPrice.toFloat()
            )
        } else {
            receiptDataViewModel.refreshProductList(
                queryStoreName,
                queryCategoryName,
                queryFromDate,
                queryToDate,
                queryLowerPrice
            )
        }
    }

    private fun initObserver() {
        receiptDataViewModel.productRichList.observe(viewLifecycleOwner) {
            it?.let {
                productListAdapter.productList = it
                productListAdapter.notifyDataSetChanged()
            }
        }
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it.map { it2 -> it2.name }
                ).also { adapter ->
                    binding.storeInput.setAdapter(adapter)
                }
            }
        }
        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it.map { it2 -> it2.name }
                ).also { adapter ->
                    binding.categoryNameInput.setAdapter(adapter)
                }
            }
        }
    }


    override fun onItemClick(index: Int) {
        receiptDataViewModel.productRichList.value?.get(index)?.let {
            val chosenProduct = Product(
                it.receiptId,
                it.name,
                it.categoryId,
                it.amount,
                it.itemPrice,
                it.finalPrice,
                it.ptuType,
                it.raw
            )
            chosenProduct.id = it.id
            receiptDataViewModel.product.value = arrayListOf(chosenProduct)
        }
        val action =
            ListingFragmentDirections.actionListingFragmentToAddProductFragment(true, 0)
        Navigation.findNavController(requireView()).navigate(action)
    }

}