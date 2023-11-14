package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentReceiptListBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReceiptListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentReceiptListBinding? = null
    private val binding get() = _binding!!

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var receiptListAdapter: ReceiptListAdapter

    private var calendarDateFrom = Calendar.getInstance()
    private var calendarDateTo = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        receiptDataViewModel.refreshReceiptList("")
        receiptDataViewModel.refreshStoreList()

        recyclerViewEvent = binding.recyclerViewEventReceipts
        receiptListAdapter = ReceiptListAdapter(
            requireContext(),
            receiptDataViewModel.receiptList.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.receiptList.value?.removeAt(i)
            receiptListAdapter.receiptList = receiptDataViewModel.receiptList.value ?: arrayListOf()
            receiptListAdapter.notifyItemRemoved(i)
        }
        recyclerViewEvent.adapter = receiptListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.refreshReceiptList(text.toString())
        }
        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            receiptDataViewModel.refreshReceiptList("")
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
        }
    }

    private fun showDatePicker() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    Pair(
                        calendarDateFrom.timeInMillis,
                        calendarDateTo.timeInMillis,
                    )
                )
                .build()
        dateRangePicker.show(childFragmentManager, "Test")

        dateRangePicker.addOnPositiveButtonClickListener {
            calendarDateFrom.timeInMillis = it.first
            calendarDateTo.timeInMillis = it.second
            val dateFrom = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(calendarDateFrom.time)
            val dateTo = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(calendarDateTo.time)
            binding.dateBetweenInput.setText("$dateFrom - $dateTo")
        }
    }

    private fun initObserver() {
        receiptDataViewModel.receiptList.observe(viewLifecycleOwner) {
            it?.let {
                receiptListAdapter.receiptList = it
                receiptListAdapter.notifyDataSetChanged()
            }
        }
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it.map { it2 -> it2.name }
                ).also { adapter ->
                    binding.storeInput.setAdapter(adapter)
                }
            }
        }
    }


    override fun onItemClick(productIndex: Int) {

        val receipt = receiptDataViewModel.receiptList.value?.get(productIndex)
        receiptDataViewModel.receipt.value = ReceiptDTO(
            receipt?.id ?: -1,
            receipt?.name,
            receipt?.nip,
            receipt?.pln.toString(),
            receipt?.ptu.toString(),
            receipt?.date,
            receipt?.time
        )
        if (receipt != null) {
            receiptDataViewModel.refreshProductList(receipt.id)
        }
        val products = receiptDataViewModel.savedProduct.value
        val listProductDTO = arrayListOf<ProductDTO>()
        products?.forEach {
            listProductDTO.add(
                ProductDTO(
                    it.id,
                    it.receiptId,
                    it.name,
                    it.finalPrice.toString(),
                    it.categoryId,
                    it.amount.toString(),
                    it.itemPrice.toString(),
                    it.ptuType,
                    ""
                )
            )
        }
        receiptDataViewModel.product

        Navigation.findNavController(binding.root)
            .navigate(R.id.action_storeListFragment_to_stageBasicInfoFragment)
    }
}