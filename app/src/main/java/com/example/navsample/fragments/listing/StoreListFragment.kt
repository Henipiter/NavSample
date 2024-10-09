package com.example.navsample.fragments.listing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.StoreListAdapter
import com.example.navsample.databinding.FragmentStoreListBinding
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.fragments.dialogs.SortingDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel


class StoreListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var storeListAdapter: StoreListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        refreshList()
        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.menu.findItem(R.id.filter).isVisible = false
        binding.toolbar.menu.findItem(R.id.collapse).isVisible = false
        binding.toolbar.menu.findItem(R.id.expand).isVisible = false
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sort -> {
                    //TODO Connect Dialog with DB
                    val selected = receiptDataViewModel.storeSort.value
                        ?: receiptDataViewModel.defaultStoreSort

                    SortingDialog(
                        selected,
                        StoreSort.entries.map { it.friendlyNameKey }) { name, dir ->
                        val appliedSort = SortProperty(StoreSort::class, name, dir)
                        receiptDataViewModel.storeSort.value = appliedSort
                        receiptDataViewModel.updateSorting(appliedSort)
                        Toast.makeText(requireContext(), "$appliedSort", Toast.LENGTH_SHORT).show()
                    }.show(childFragmentManager, "Test")
                    true
                }

                else -> false
            }
        }
        recyclerViewEvent = binding.recyclerViewEventReceipts
        storeListAdapter = StoreListAdapter(
            requireContext(),
            receiptDataViewModel.storeList.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.storeList.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "$i Are you sure you want to delete the store with dependent receipts and" +
                            " products??\n\n" + "Name: " + it.name + "\nNIP: " + it.nip
                ) {
                    receiptDataViewModel.deleteStore(it)
                    receiptDataViewModel.storeList.value?.removeAt(i)
                    storeListAdapter.notifyItemRemoved(i)
                    storeListAdapter.notifyItemRangeChanged(i, storeListAdapter.storeList.size)

                }.show(childFragmentManager, "TAG")
            }
        }
        recyclerViewEvent.adapter = storeListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.storeNameInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterStoreList.value?.store = text.toString()
            refreshList()
        }
        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterStoreList.value?.nip = text.toString()
            refreshList()
        }
        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            receiptDataViewModel.filterStoreList.value?.store = ""
        }
        binding.storeNIPLayout.setStartIconOnClickListener {
            binding.storeNIPInput.setText("")
            receiptDataViewModel.filterStoreList.value?.nip = ""
        }

        binding.newButton.setOnClickListener {
            receiptDataViewModel.savedStore.value = null
            val action =
                ListingFragmentDirections.actionListingFragmentToAddStoreFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    override fun onItemClick(index: Int) {
        receiptDataViewModel.storeList.value?.let { storeList ->
            val store = storeList[index]
            receiptDataViewModel.savedStore.value = store
            receiptDataViewModel.store.value = store
            val action =
                ListingFragmentDirections.actionListingFragmentToAddStoreFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }


    }

    private fun putFilterDefinitionIntoInputs() {
        receiptDataViewModel.filterStoreList.value?.let {
            if (binding.storeNameInput.text.toString() != it.store) {
                binding.storeNameInput.setText(it.store)
            }
            if (binding.storeNIPInput.text.toString() != it.nip) {
                binding.storeNIPInput.setText(it.nip)
            }

        }
    }

    private fun refreshList() { //TODO move to viewModel
        receiptDataViewModel.filterStoreList.value?.let {
            putFilterDefinitionIntoInputs()
            receiptDataViewModel.refreshStoreList(it.store, it.nip)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                storeListAdapter.storeList = it
                storeListAdapter.notifyDataSetChanged()
            }
        }
        receiptDataViewModel.filterStoreList.observe(viewLifecycleOwner) {
            refreshList()
        }
    }
}
