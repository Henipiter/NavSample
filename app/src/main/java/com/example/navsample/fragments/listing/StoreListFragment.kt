package com.example.navsample.fragments.listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.adapters.StoreListAdapter
import com.example.navsample.databinding.FragmentStoreListBinding
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
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

        recyclerViewEvent = binding.recyclerViewEventReceipts
        storeListAdapter = StoreListAdapter(
            requireContext(),
            receiptDataViewModel.storeList.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.storeList.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the store with dependent receipts and" +
                            " products??\n\n" + "Name: " + it.name + "\nNIP: " + it.nip
                ) {
                    receiptDataViewModel.deleteStore(it)
                    receiptDataViewModel.storeList.value?.removeAt(i)
                    storeListAdapter.storeList =
                        receiptDataViewModel.storeList.value ?: arrayListOf()
                    storeListAdapter.notifyItemRemoved(i)
                }.show(childFragmentManager, "TAG")
            }
        }
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
        recyclerViewEvent.adapter = storeListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.newButton.setOnClickListener {
            receiptDataViewModel.savedStore.value = null
            val action =
                ListingFragmentDirections.actionListingFragmentToAddStoreFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    override fun onItemClick(index: Int) {
        val store = receiptDataViewModel.storeList.value!![index]
        receiptDataViewModel.savedStore.value = store
        receiptDataViewModel.store.value = store
        val action =
            ListingFragmentDirections.actionListingFragmentToAddStoreFragment()
        Navigation.findNavController(requireView()).navigate(action)

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

    private fun refreshList() {
        receiptDataViewModel.filterStoreList.value?.let {
            putFilterDefinitionIntoInputs()
            receiptDataViewModel.refreshStoreList(it.store, it.nip)
        }
    }

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
