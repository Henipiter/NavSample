package com.example.navsample.fragments.filter

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
import com.example.navsample.R
import com.example.navsample.databinding.FragmentFilterReceiptListBinding
import com.example.navsample.dto.filter.FilterReceiptList
import com.example.navsample.viewmodels.ListingViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class FilterReceiptListFragment : Fragment() {
    private var _binding: FragmentFilterReceiptListBinding? = null
    private val binding get() = _binding!!
    private val listingViewModel: ListingViewModel by activityViewModels()

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

        binding.toolbar.inflateMenu(R.menu.top_menu_filter)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.title = "Filter"

        initObserver()
        putFilterDefinitionIntoInputs()
        setUpCalendars()

        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.dateBetweenInput.setOnClickListener {
            showDatePicker()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            listingViewModel.filterReceiptList.value?.store = ""
        }

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            listingViewModel.filterReceiptList.value?.store = text.toString()
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
            listingViewModel.filterReceiptList.value?.let {
                it.dateFrom = ""
                it.dateTo = ""
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear_filter -> {
                    binding.dateBetweenInput.setText("")
                    binding.storeInput.setText("")
                    listingViewModel.filterReceiptList.value = FilterReceiptList()
                    true
                }

                R.id.confirm -> {
                    listingViewModel.filterReceiptList.value = filterReceiptList
                    listingViewModel.loadDataByReceiptFilter()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
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
            listingViewModel.filterReceiptList.value?.let { filter ->
                filter.dateFrom = getDateFormat().format(calendarDateFrom.time)
                filter.dateTo = getDateFormat().format(calendarDateTo.time)
                val text = "${filter.dateFrom} - ${filter.dateTo}"
                binding.dateBetweenInput.setText(text)
            }
        }
    }

    private fun putFilterDefinitionIntoInputs() {
        binding.storeInput.setText(filterReceiptList.store)
        val text = filterReceiptList.dateFrom + " - " + filterReceiptList.dateTo
        binding.dateBetweenInput.setText(text)
    }

    private fun getDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf
    }

    private fun initObserver() {
        listingViewModel.storeList.observe(viewLifecycleOwner) {
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
        listingViewModel.filterReceiptList.observe(viewLifecycleOwner) {
            it?.let {
                filterReceiptList = it
                putFilterDefinitionIntoInputs()
                setUpCalendars()

            }
        }
    }

    private fun setUpCalendars() {
        try {
            getDateFormat().parse(filterReceiptList.dateFrom)?.let { date ->
                calendarDateFrom.time = date
            }
        } catch (_: Exception) {
        }

        try {
            getDateFormat().parse(filterReceiptList.dateTo)?.let { date ->
                calendarDateTo.time = date
            }
        } catch (_: Exception) {
        }
    }
}