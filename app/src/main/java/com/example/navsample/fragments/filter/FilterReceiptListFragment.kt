package com.example.navsample.fragments.filter

import android.R
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
import com.example.navsample.databinding.FragmentFilterReceiptListBinding
import com.example.navsample.dto.filter.FilterReceiptList
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterReceiptListFragment : Fragment() {
    private var _binding: FragmentFilterReceiptListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var filterReceiptList = FilterReceiptList()
    private var calendarDateFrom = Calendar.getInstance()
    private var calendarDateTo = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFilterReceiptListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        putFilterDefinitionIntoInputs()

        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.dateBetweenInput.setOnClickListener {
            showDatePicker()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            receiptDataViewModel.filterReceiptList.value?.store = ""
            receiptDataViewModel.refreshReceiptList("")
        }

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterReceiptList.value?.store = text.toString()
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
            receiptDataViewModel.filterReceiptList.value?.let {
                it.dateFrom = ""
                it.dateTo = ""
            }
        }

        binding.confirmButton.setOnClickListener {
            receiptDataViewModel.filterReceiptList.value = filterReceiptList
            Navigation.findNavController(it).popBackStack()
        }
        binding.clearFilterButton.setOnClickListener {
            binding.dateBetweenInput.setText("")
            binding.storeInput.setText("")
            receiptDataViewModel.filterReceiptList.value = FilterReceiptList()
            Navigation.findNavController(it).popBackStack()
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
            receiptDataViewModel.filterReceiptList.value?.let { filter ->
                filter.dateFrom = getDateFormat().format(calendarDateFrom.time)
                filter.dateTo = getDateFormat().format(calendarDateTo.time)

                binding.dateBetweenInput.setText("${filter.dateFrom} - ${filter.dateTo}")
            }
        }
    }

    private fun putFilterDefinitionIntoInputs() {
        binding.storeInput.setText(filterReceiptList.store)
        binding.dateBetweenInput.setText(filterReceiptList.dateFrom + " - " + filterReceiptList.dateTo)
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private fun initObserver() {
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
        receiptDataViewModel.filterReceiptList.observe(viewLifecycleOwner) {
            it?.let {
                filterReceiptList = it
                putFilterDefinitionIntoInputs()
            }
        }
    }
}