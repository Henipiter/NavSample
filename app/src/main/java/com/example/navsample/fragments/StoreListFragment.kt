package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentStoreListBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel

class StoreListFragment : Fragment(), ItemClickListener{
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = _binding!!

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var receiptListAdapter: ReceiptListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        receiptDataViewModel.refreshReceiptList()

        recyclerViewEvent = binding.recyclerViewEventReceipts
        receiptListAdapter = ReceiptListAdapter(
            requireContext(),
            receiptDataViewModel.receiptList.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.product.value?.removeAt(i)
            receiptListAdapter.receiptList = receiptDataViewModel.receiptList.value ?: arrayListOf()
            receiptListAdapter.notifyDataSetChanged()
        }
        recyclerViewEvent.adapter = receiptListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)




    }

    private fun initObserver(){
        receiptDataViewModel.receiptList.observe(viewLifecycleOwner) {
            it?.let {

                receiptListAdapter.receiptList = it
                receiptListAdapter.notifyDataSetChanged()
            }
        }
    }


    override fun onItemClick(productIndex: Int) {

        Toast.makeText(requireContext(), "AAA",Toast.LENGTH_SHORT).show()
    }
}