package com.example.navsample.fragments.listing


import android.annotation.SuppressLint
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
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.fragments.dialogs.SortingDialog
import com.example.navsample.sheets.ProductBottomSheetFragment
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.factory.AddProductDataViewModelFactory
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel

class ProductListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels() {
        AddProductDataViewModelFactory(requireActivity().application)
    }
    private val listingViewModel: ListingViewModel by activityViewModels()

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
        defineToolbar()

        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.setOnRefreshListener {
            refreshList()
        }

        recyclerViewEvent = binding.recyclerViewEventProducts
        richProductListAdapter = RichProductListAdapter(
            requireContext(), listingViewModel.productRichList.value ?: arrayListOf(), this
        ) { index ->
            listingViewModel.productRichList.value?.get(index)?.let { productRichData ->
                popUpButtonSheet(index, productRichData)
            }
        }
        recyclerViewEvent.adapter = richProductListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        listingViewModel.refreshStoreList()
        listingViewModel.refreshCategoryList()
    }

    private fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refresh -> {
                    refreshList()
                    true
                }

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
                    val selected = listingViewModel.richProductSort.value
                        ?: listingViewModel.defaultRichProductSort
                    SortingDialog(
                        selected,
                        RichProductSort.entries.map { sort -> sort.friendlyNameKey }) { name, dir ->
                        val appliedSort = SortProperty(RichProductSort::class, name, dir)
                        listingViewModel.richProductSort.value = appliedSort
                        listingViewModel.updateSorting(appliedSort)
                    }.show(childFragmentManager, "Test")
                    true
                }

                else -> false
            }
        }
    }

    private fun refreshList() {
        listingViewModel.loadDataByProductFilter()
    }

    private fun popUpButtonSheet(index: Int, productRichData: ProductRichData) {
        val modalBottomSheet = ProductBottomSheetFragment(
            { onDelete(index, productRichData) },
            onJumpToCategory = { onJumpToCategory(productRichData.categoryId) },
            onJumpToStore = { onJumpToStore(productRichData.storeId) },
            onJumpToReceipt = { onJumpToReceipt(productRichData.receiptId) }
        )
        modalBottomSheet.show(parentFragmentManager, ProductBottomSheetFragment.TAG)
    }

    private fun onJumpToReceipt(receiptId: String) {
        val action = ListingFragmentDirections.actionListingFragmentToAddReceiptFragment(
            inputType = AddingInputType.ID.name,
            receiptId = receiptId,
            sourceFragment = FragmentName.RECEIPT_LIST_FRAGMENT,
            storeId = ""
        )
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun onJumpToStore(storeId: String) {
        val action =
            ListingFragmentDirections.actionListingFragmentToAddStoreFragment(
                inputType = AddingInputType.ID.name,
                storeId = storeId,
                storeName = null,
                storeNip = null,
                sourceFragment = FragmentName.STORE_LIST_FRAGMENT,
                categoryId = ""
            )
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun onJumpToCategory(categoryId: String) {
        val action = ListingFragmentDirections.actionListingFragmentToAddCategoryFragment(
            categoryId = categoryId,
            inputType = AddingInputType.ID.name,
            sourceFragment = FragmentName.CATEGORY_LIST_FRAGMENT
        )
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun onDelete(index: Int, productRichData: ProductRichData) {
        ConfirmDialog(
            getString(R.string.delete_confirmation_title),
            getString(R.string.delete_product_confirmation_dialog)
        ) {
            if (productRichData.id.isEmpty()) {
                Toast.makeText(requireContext(), "Unexpected error", Toast.LENGTH_SHORT).show()
                return@ConfirmDialog
            }
            addProductDataViewModel.deleteProduct(productRichData.id) {
                listingViewModel.reloadOutdatedProductRichList.postValue(true)
                listingViewModel.reloadOutdatedProductTagList.postValue(true)

                listingViewModel.productRichList.value?.let { productRichList ->
                    productRichList.removeAt(index)
                    richProductListAdapter.productList = productRichList
                    richProductListAdapter.notifyItemRemoved(index)
                    richProductListAdapter.notifyItemRangeChanged(
                        index, richProductListAdapter.productList.size
                    )
                }
                listingViewModel.loadDataByReceiptFilter()
            }


        }.show(childFragmentManager, "TAG")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        listingViewModel.productRichList.observe(viewLifecycleOwner) {
            it?.let {
                richProductListAdapter.productList = it
                richProductListAdapter.notifyDataSetChanged()
                binding.refreshLayout.isRefreshing = false
            }
        }
    }

    override fun onItemClick(index: Int) {
        listingViewModel.productRichList.value?.get(index)?.let {
            val chosenProduct = Product(
                it.receiptId,
                it.name,
                it.categoryId,
                it.quantity,
                it.unitPrice,
                it.subtotalPrice,
                0,
                it.subtotalPrice,
                it.ptuType,
                it.raw,
                it.validPrice
            )
            chosenProduct.id = it.id
            val action =
                ListingFragmentDirections.actionListingFragmentToAddProductFragment(
                    inputType = AddingInputType.ID.name,
                    productId = it.id,
                    receiptId = it.receiptId,
                    storeId = it.storeId,
                    tagId = "",
                    sourceFragment = FragmentName.PRODUCT_LIST_FRAGMENT,
                    categoryId = ""
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

}