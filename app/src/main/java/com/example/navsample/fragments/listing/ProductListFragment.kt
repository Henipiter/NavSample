package com.example.navsample.fragments.listing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.RichProductListAdapter
import com.example.navsample.databinding.FragmentProductListBinding
import com.example.navsample.entities.Product
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel

class ProductListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var richProductListAdapter: RichProductListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        putFilterDefinitionIntoInputs()

        refreshList()

        recyclerViewEvent = binding.recyclerViewEventProducts
        richProductListAdapter = RichProductListAdapter(
            requireContext(),
            receiptDataViewModel.productRichList.value ?: arrayListOf(), this

        ) { i ->
            receiptDataViewModel.product.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the product??\n\nName: " + it.name +
                            "\nPLN: " + it.subtotalPrice
                ) {
                    if (it.id != null && it.id!! >= 0) {
                        receiptDataViewModel.deleteProduct(it.id!!)
                    }
                    receiptDataViewModel.productRichList.value?.removeAt(i)
                    richProductListAdapter.productList =
                        receiptDataViewModel.productRichList.value ?: arrayListOf()
                    richProductListAdapter.notifyDataSetChanged()
                }.show(childFragmentManager, "TAG")
            }
        }
        recyclerViewEvent.adapter = richProductListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.filterLayout.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_listingFragment_to_filterProductListFragment)
        }
        receiptDataViewModel.refreshStoreList()
        receiptDataViewModel.refreshCategoryList()
    }

    private fun initObserver() {
        receiptDataViewModel.productRichList.observe(viewLifecycleOwner) {
            it?.let {
                richProductListAdapter.productList = it
                richProductListAdapter.notifyDataSetChanged()
            }
        }
        receiptDataViewModel.filterProductList.observe(viewLifecycleOwner) {
            putFilterDefinitionIntoInputs()
            refreshList()
        }
    }

    private fun convertDoubleToText(double: Double): String {
        if (double < 0.0) {
            return ""
        }
        return double.toString()
    }

    private fun putFilterDefinitionIntoInputs() {
        receiptDataViewModel.filterProductList.value?.let {
            binding.filterStoreCard.text = if (it.store == "") "-" else it.store
            binding.filterCategoryCard.text = if (it.category == "") "-" else it.category
            binding.filterPriceCard.text =
                "${convertDoubleToText(it.lowerPrice)} - ${convertDoubleToText(it.higherPrice)}"
            binding.filterDateCard.text = "${it.dateFrom} - ${it.dateTo}"
        }
    }

    override fun onItemClick(index: Int) {
        receiptDataViewModel.productRichList.value?.get(index)?.let {
            val chosenProduct = Product(
                it.receiptId,
                it.name,
                it.categoryId,
                it.quantity,
                it.unitPrice,
                it.subtotalPrice,
                0.0,
                it.subtotalPrice,
                it.ptuType,
                it.raw
            )
            chosenProduct.id = it.id
            receiptDataViewModel.product.value = arrayListOf(chosenProduct)
            receiptDataViewModel.getReceiptById(chosenProduct.receiptId)
            receiptDataViewModel.getStoreById(it.storeId)
        }
        val action =
            ListingFragmentDirections.actionListingFragmentToAddProductFragment(true, 0)
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun refreshList() {
        receiptDataViewModel.filterProductList.value?.let {
            if (it.higherPrice != -1.0) {
                receiptDataViewModel.refreshProductList(
                    it.store,
                    it.category,
                    it.dateFrom,
                    it.dateTo,
                    it.lowerPrice,
                    it.higherPrice
                )
            } else {
                receiptDataViewModel.refreshProductList(
                    it.store,
                    it.category,
                    it.dateFrom,
                    it.dateTo,
                    it.lowerPrice
                )
            }

        }
    }
}