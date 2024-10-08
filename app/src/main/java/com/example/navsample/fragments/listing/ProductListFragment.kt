package com.example.navsample.fragments.listing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.RichProductListAdapter
import com.example.navsample.databinding.FragmentProductListBinding
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.entities.Product
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.fragments.dialogs.SortingDialog
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

        refreshList()
        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.collapse -> {
                    for (i in 0..richProductListAdapter.productList.lastIndex) {
                        if (!richProductListAdapter.productList[i].collapse) {
                            richProductListAdapter.productList[i].collapse = true
                            richProductListAdapter.notifyItemChanged(i)
                        }
                    }
                    true
                }

                R.id.expand -> {
                    for (i in 0..richProductListAdapter.productList.lastIndex) {
                        if (richProductListAdapter.productList[i].collapse) {
                            richProductListAdapter.productList[i].collapse = false
                            richProductListAdapter.notifyItemChanged(i)
                        }
                    }
                    true
                }

                R.id.filter -> {
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_listingFragment_to_filterProductListFragment)
                    true
                }

                R.id.sort -> {
                    //TODO Connect Dialog with DB
                    SortingDialog(RichProductSort.entries.map { it.friendlyNameKey }) { name, dir ->
                        val appliedSort = SortProperty(RichProductSort::class, name, dir)
                        receiptDataViewModel.richProductSort.value = appliedSort
                        receiptDataViewModel.updateSorting(appliedSort)
                        Toast.makeText(requireContext(), "$appliedSort", Toast.LENGTH_SHORT).show()
                    }.show(childFragmentManager, "Test")
                    true
                }

                else -> false
            }
        }
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
            refreshList()
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

    private fun refreshList() { //TODO move to viewModel
        receiptDataViewModel.filterProductList.value?.let {
            receiptDataViewModel.refreshProductList(
                it.store,
                it.category,
                it.dateFrom,
                it.dateTo,
                it.lowerPrice,
                it.higherPrice
            )
        }
    }
}