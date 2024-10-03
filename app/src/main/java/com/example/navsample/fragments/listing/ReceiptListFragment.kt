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
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentReceiptListBinding
import com.example.navsample.dto.sort.Direction
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.entities.Receipt
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel

class ReceiptListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentReceiptListBinding? = null
    private val binding get() = _binding!!

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

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
        refreshList()
        receiptDataViewModel.refreshCategoryList()
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
                    val appliedSort = SortProperty(ReceiptWithStoreSort.DATE, Direction.ASCENDING)
                    receiptDataViewModel.receiptWithStoreSort.value = appliedSort
                    receiptDataViewModel.updateSorting(appliedSort)

                    Toast.makeText(requireContext(), "sort", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        recyclerViewEvent = binding.recyclerViewEventReceipts
        receiptListAdapter = ReceiptListAdapter(
            requireContext(),
            receiptDataViewModel.receiptList.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.receiptList.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the receipt with dependent products??\n\n" +
                            "Store: " + it.name
                            + "\nPLN: " + it.pln
                            + "\nPTU: " + it.ptu
                            + "\nDate: " + it.date
                            + "\nTime: " + it.time
                ) {

                    receiptDataViewModel.deleteReceipt(it.id)
                    receiptDataViewModel.receiptList.value?.removeAt(i)
                    receiptListAdapter.receiptList =
                        receiptDataViewModel.receiptList.value ?: arrayListOf()
                    receiptListAdapter.notifyItemRemoved(i)

                }.show(childFragmentManager, "TAG")
            }

        }
        recyclerViewEvent.adapter = receiptListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun refreshList() { //TODO move to viewModel
        receiptDataViewModel.filterReceiptList.value?.let {
            receiptDataViewModel.refreshReceiptList(
                it.store, it.dateFrom, it.dateTo
            )
        }
    }

    private fun initObserver() {
        receiptDataViewModel.receiptList.observe(viewLifecycleOwner) {
            it?.let {
                receiptListAdapter.receiptList = it
                receiptListAdapter.notifyDataSetChanged()
            }
        }

        receiptDataViewModel.filterReceiptList.observe(viewLifecycleOwner) {
            refreshList()
        }
    }

    override fun onItemClick(index: Int) {
        receiptDataViewModel.receiptList.value?.get(index)?.let {
            receiptDataViewModel.getStoreById(it.storeId)
            val receipt = Receipt(
                it.storeId,
                it.pln.toString().toDouble(),
                it.ptu.toString().toDouble(),
                it.date,
                it.time
            )
            receipt.id = it.id
            receiptDataViewModel.receipt.value = receipt
        }

        Navigation.findNavController(binding.root)
            .navigate(R.id.action_listingFragment_to_addReceiptFragment)
    }
}
