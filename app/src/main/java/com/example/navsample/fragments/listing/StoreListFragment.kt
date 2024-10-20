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
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.fragments.dialogs.SortingDialog
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddStoreDataViewModel


class StoreListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = _binding!!
    private val addStoreDataViewModel: AddStoreDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

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
        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.menu.findItem(R.id.filter).isVisible = false
        binding.toolbar.menu.findItem(R.id.collapse).isVisible = false
        binding.toolbar.menu.findItem(R.id.expand).isVisible = false
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sort -> {
                    //TODO Connect Dialog with DB
                    val selected = listingViewModel.storeSort.value
                        ?: listingViewModel.defaultStoreSort

                    SortingDialog(
                        selected,
                        StoreSort.entries.map { sort -> sort.friendlyNameKey }) { name, dir ->
                        val appliedSort = SortProperty(StoreSort::class, name, dir)
                        listingViewModel.storeSort.value = appliedSort
                        listingViewModel.updateSorting(appliedSort)
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
            listingViewModel.storeList.value ?: arrayListOf(), this
        ) { i: Int ->
            listingViewModel.storeList.value?.get(i)?.let {
                if (it.id == null) {
                    Toast.makeText(requireContext(), "STORE HAS NOT ID", Toast.LENGTH_SHORT).show()
                } else {
                    ConfirmDialog(
                        "Delete",
                        "$i Are you sure you want to delete the store with dependent receipts and" +
                                " products??\n\n" + "Name: " + it.name + "\nNIP: " + it.nip
                    ) {
                        addStoreDataViewModel.deleteStore(it.id!!)
                        listingViewModel.storeList.value?.let { storeList ->
                            storeList.removeAt(i)
                            storeListAdapter.storeList = storeList
                            storeListAdapter.notifyItemRemoved(i)
                            storeListAdapter.notifyItemRangeChanged(
                                i, storeListAdapter.storeList.size
                            )
                        }
                        listingViewModel.loadDataByProductFilter()
                        listingViewModel.loadDataByReceiptFilter()
                    }.show(childFragmentManager, "TAG")
                }
            }
        }
        recyclerViewEvent.adapter = storeListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.storeNameInput.doOnTextChanged { text, _, _, _ ->
            listingViewModel.filterStoreList.value?.store = text.toString()
            listingViewModel.loadDataByStoreFilter()
        }
        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            listingViewModel.filterStoreList.value?.nip = text.toString()
            listingViewModel.loadDataByStoreFilter()
        }
        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            listingViewModel.filterStoreList.value?.store = ""
        }
        binding.storeNIPLayout.setStartIconOnClickListener {
            binding.storeNIPInput.setText("")
            listingViewModel.filterStoreList.value?.nip = ""
        }

        binding.newButton.setOnClickListener {
            val action =
                ListingFragmentDirections.actionListingFragmentToAddStoreFragment(
                    storeName = null,
                    storeNip = null
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    override fun onItemClick(index: Int) {
        listingViewModel.storeList.value?.let { storeList ->
            val storeId = storeList[index].id
            if (storeId == null) {
                Toast.makeText(requireContext(), "STORE ID IS NULL", Toast.LENGTH_SHORT).show()

            } else {
                val action =
                    ListingFragmentDirections.actionListingFragmentToAddStoreFragment(
                        inputType = AddingInputType.ID.name,
                        storeId = storeId,
                        storeName = null,
                        storeNip = null
                    )
                Navigation.findNavController(requireView()).navigate(action)
            }
        }


    }

    private fun putFilterDefinitionIntoInputs() {
        listingViewModel.filterStoreList.value?.let {
            if (binding.storeNameInput.text.toString() != it.store) {
                binding.storeNameInput.setText(it.store)
            }
            if (binding.storeNIPInput.text.toString() != it.nip) {
                binding.storeNIPInput.setText(it.nip)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        listingViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                storeListAdapter.storeList = it
                storeListAdapter.notifyDataSetChanged()
            }
        }
        listingViewModel.filterStoreList.observe(viewLifecycleOwner) {
            putFilterDefinitionIntoInputs()
        }
    }
}
