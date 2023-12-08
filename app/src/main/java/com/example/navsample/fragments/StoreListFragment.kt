package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.StoreDTO
import com.example.navsample.ItemClickListener
import com.example.navsample.adapters.StoreListAdapter
import com.example.navsample.databinding.FragmentStoreListBinding
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


        receiptDataViewModel.refreshStoreList()

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
        recyclerViewEvent.adapter = storeListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.newButton.setOnClickListener {
            val action =
                StoreListFragmentDirections.actionStoreListFragmentToEditStoreFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    override fun onItemClick(storeIndex: Int) {
        val store = receiptDataViewModel.storeList.value!![storeIndex]
        receiptDataViewModel.savedStore.value = store
        receiptDataViewModel.store.value = StoreDTO(store.name, store.nip)
        val action =
            StoreListFragmentDirections.actionStoreListFragmentToEditStoreFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    private fun initObserver() {
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                storeListAdapter.storeList = it
                storeListAdapter.notifyDataSetChanged()
            }
        }
    }
}
