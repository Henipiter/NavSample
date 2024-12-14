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
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentReceiptListBinding
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.fragments.dialogs.SortingDialog
import com.example.navsample.sheets.ReceiptBottomSheetFragment
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddReceiptDataViewModel

class ReceiptListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentReceiptListBinding? = null
    private val binding get() = _binding!!

    private val addReceiptDataViewModel: AddReceiptDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var receiptListAdapter: ReceiptListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReceiptListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.menu.findItem(R.id.collapse).isVisible = false
        binding.toolbar.menu.findItem(R.id.expand).isVisible = false
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.filter -> {
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_listingFragment_to_filterReceiptListFragment)
                    true
                }

                R.id.sort -> {
                    //TODO Connect Dialog with DB
                    val selected = listingViewModel.receiptWithStoreSort.value
                        ?: listingViewModel.defaultReceiptWithStoreSort
                    SortingDialog(
                        selected,
                        ReceiptWithStoreSort.entries.map { sort -> sort.friendlyNameKey }) { name, dir ->
                        val appliedSort = SortProperty(ReceiptWithStoreSort::class, name, dir)
                        listingViewModel.receiptWithStoreSort.value = appliedSort
                        listingViewModel.updateSorting(appliedSort)
                        Toast.makeText(requireContext(), "$appliedSort", Toast.LENGTH_SHORT).show()
                    }.show(childFragmentManager, "Test")
                    true
                }

                else -> false
            }
        }


        binding.newButton.setOnClickListener {
            val action =
                ListingFragmentDirections.actionListingFragmentToImageImportFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        recyclerViewEvent = binding.recyclerViewEventReceipts
        receiptListAdapter = ReceiptListAdapter(
            requireContext(),
            listingViewModel.receiptList.value ?: arrayListOf(), this
        ) { index: Int ->
            listingViewModel.receiptList.value?.get(index)?.let { receiptWithStore ->
                popUpButtonSheet(index, receiptWithStore)
            }
        }
        recyclerViewEvent.adapter = receiptListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun popUpButtonSheet(index: Int, receiptWithStore: ReceiptWithStore) {
        val modalBottomSheet = ReceiptBottomSheetFragment(
            { onDelete(index, receiptWithStore) },
            { onJumpToStore(receiptWithStore.storeId) }
        )
        modalBottomSheet.show(parentFragmentManager, ReceiptBottomSheetFragment.TAG)
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

    private fun onDelete(index: Int, receiptWithStore: ReceiptWithStore) {
        ConfirmDialog(
            "Delete",
            "Are you sure you want to delete the receipt with dependent products??\n\n" +
                    "Store: " + receiptWithStore.name
                    + "\nPLN: " + intPriceToString(receiptWithStore.pln)
                    + "\nPTU: " + intPriceToString(receiptWithStore.ptu)
                    + "\nDate: " + receiptWithStore.date
                    + "\nTime: " + receiptWithStore.time
        ) {
            addReceiptDataViewModel.deleteReceipt(receiptWithStore.id)
            listingViewModel.receiptList.value?.let { receiptList ->
                receiptList.removeAt(index)
                receiptListAdapter.receiptList = receiptList
                receiptListAdapter.notifyItemRemoved(index)
                receiptListAdapter.notifyItemRangeChanged(
                    index, receiptListAdapter.receiptList.size
                )
            }
            listingViewModel.loadDataByProductFilter()


        }.show(childFragmentManager, "TAG")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        listingViewModel.receiptList.observe(viewLifecycleOwner) {
            it?.let {
                receiptListAdapter.receiptList = it
                receiptListAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClick(index: Int) {
        listingViewModel.receiptList.value?.let { receiptList ->
            val receiptId = receiptList[index].id
            val action = ListingFragmentDirections.actionListingFragmentToAddReceiptFragment(
                inputType = AddingInputType.ID.name,
                receiptId = receiptId,
                sourceFragment = FragmentName.RECEIPT_LIST_FRAGMENT,
                storeId = ""
            )
            Navigation.findNavController(requireView()).navigate(action)

        }
    }
}
