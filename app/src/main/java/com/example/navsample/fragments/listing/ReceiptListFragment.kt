package com.example.navsample.fragments.listing

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
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentReceiptListBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
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

    var dateFrom = ""
    var dateTo = ""
    var text = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
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

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            this.text = text.toString()
            refreshList()

        }
        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
            refreshList()
        }
        binding.dateBetweenInput.setOnClickListener {
            showDatePicker()
            refreshList()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            text = ""
            refreshList()
            receiptDataViewModel.refreshReceiptList("")
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
            dateTo = ""
            dateFrom = ""
            refreshList()
        }
    }

    private fun refreshList() {
        if (dateTo == "" && dateFrom == "") {
            receiptDataViewModel.refreshReceiptList(text)
        } else {
            receiptDataViewModel.refreshReceiptList(text, dateFrom, dateTo)
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
            dateFrom = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendarDateFrom.time)
            dateTo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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


    override fun onItemClick(index: Int) {
        receiptDataViewModel.receiptList.value?.get(index)?.let {
            receiptDataViewModel.getStoreById(it.storeId)
            receiptDataViewModel.receipt.value = Receipt(
                it.id,
                it.pln.toString().toFloat(),
                it.ptu.toString().toFloat(),
                it.date,
                it.time
            )
        }

        Navigation.findNavController(binding.root)
            .navigate(R.id.action_listingFragment_to_addReceiptFragment)
    }
}
