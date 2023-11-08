package com.example.navsample.fragments

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.adapters.ReceiptListAdapter
import com.example.navsample.databinding.FragmentStoreListBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StoreListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = _binding!!

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var receiptListAdapter: ReceiptListAdapter

    private var calendarDateFrom = Calendar.getInstance()
    private var calendarDateTo = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreListBinding.inflate(inflater, container, false)
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
            receiptListAdapter.notifyDataSetChanged()
        }
        recyclerViewEvent.adapter = receiptListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.storeInput.doOnTextChanged { text, _, _, _ ->

            receiptDataViewModel.refreshReceiptList(text.toString())
        }

        binding.addDateFrom.setOnClickListener {
            showDatePicker(calendarDateFrom)
        }
        binding.addDateTo.setOnClickListener {
            showDatePicker(calendarDateTo)
        }
        binding.clearDateFrom.setOnClickListener {
            binding.dateFromInput.setText("")
            calendarDateFrom = Calendar.getInstance()
        }
        binding.addDateTo.setOnClickListener {
            binding.dateToInput.setText("")
            calendarDateFrom = Calendar.getInstance()
        }


    }

    private fun showDatePicker(calendar: Calendar) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.dateFromInput.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONDAY),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        datePickerDialog.show()
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
                    R.layout.simple_list_item_1,
                    it.map { it2 -> it2.name }
                ).also { adapter ->
                    binding.storeInput.setAdapter(adapter)
                }
            }
        }
    }


    override fun onItemClick(productIndex: Int) {

        Toast.makeText(requireContext(), "AAA",Toast.LENGTH_SHORT).show()
    }
}