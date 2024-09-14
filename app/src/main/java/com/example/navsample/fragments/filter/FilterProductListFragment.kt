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
import com.example.navsample.databinding.FragmentFilterProductListBinding
import com.example.navsample.dto.filter.FilterProductList
import com.example.navsample.fragments.dialogs.PricePickerDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterProductListFragment : Fragment() {
    private var _binding: FragmentFilterProductListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var filterProductList = FilterProductList()
    private var calendarDateFrom = Calendar.getInstance()
    private var calendarDateTo = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFilterProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.inflateMenu(R.menu.top_menu_filter)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.title = "Filter"

        initObserver()
        putFilterDefinitionIntoInputs()

        binding.priceBetweenLayout.setEndIconOnClickListener {
            showPricePicker()
        }
        binding.priceBetweenInput.setOnClickListener {
            showPricePicker()
        }
        binding.dateBetweenLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.dateBetweenInput.setOnClickListener {
            showDatePicker()
        }
        binding.storeLayout.setStartIconOnClickListener {
            binding.storeInput.setText("")
            receiptDataViewModel.filterProductList.value?.store = ""
        }

        binding.storeInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterProductList.value?.store = text.toString()
        }
        binding.categoryNameLayout.setStartIconOnClickListener {
            binding.categoryNameInput.setText("")
            receiptDataViewModel.filterProductList.value?.category = ""
        }
        binding.categoryNameInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterProductList.value?.category = text.toString()
        }
        binding.dateBetweenLayout.setStartIconOnClickListener {
            binding.dateBetweenInput.setText("")
            receiptDataViewModel.filterProductList.value?.let {
                it.dateFrom = ""
                it.dateTo = ""
            }
        }
        binding.priceBetweenLayout.setStartIconOnClickListener {
            binding.priceBetweenInput.setText("-")
            receiptDataViewModel.filterProductList.value?.let {
                it.lowerPrice = -1.0
                it.higherPrice = -1.0
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear_filter -> {
                    binding.dateBetweenInput.setText("")
                    binding.priceBetweenInput.setText("-")
                    binding.categoryNameInput.setText("")
                    binding.storeInput.setText("")
                    receiptDataViewModel.filterProductList.value = FilterProductList()
                    true
                }

                R.id.confirm -> {
                    receiptDataViewModel.filterProductList.value = filterProductList
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }
    }

    private fun convertTextToDouble(string: String): Double {
        return try {
            string.toDouble()
        } catch (e: NumberFormatException) {
            -1.0
        }
    }

    private fun convertDoubleToText(double: Double): String {
        if (double < 0.0) {
            return ""
        }
        return double.toString()
    }

    private fun showPricePicker() {
        val priceBoundaries =
            binding.priceBetweenInput.text.toString().replace("\\s+".toRegex(), "").split("-")
        PricePickerDialog(priceBoundaries[0], priceBoundaries[1]) { lower, higher ->
            receiptDataViewModel.filterProductList.value?.let {
                it.lowerPrice = convertTextToDouble(lower)
                it.higherPrice = convertTextToDouble(higher)
                binding.priceBetweenInput.setText(
                    convertDoubleToText(it.lowerPrice) + " - " + convertDoubleToText(
                        it.higherPrice
                    )
                )
            }
        }.show(childFragmentManager, "TAG")

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
            receiptDataViewModel.filterProductList.value?.let { filter ->
                filter.dateFrom = getDateFormat().format(calendarDateFrom.time)
                filter.dateTo = getDateFormat().format(calendarDateTo.time)

                binding.dateBetweenInput.setText("${filter.dateFrom} - ${filter.dateTo}")
            }
        }
    }

    private fun putFilterDefinitionIntoInputs() {
        binding.storeInput.setText(filterProductList.store)
        binding.categoryNameInput.setText(filterProductList.category)
        binding.priceBetweenInput.setText(
            convertDoubleToText(filterProductList.lowerPrice) + " - " + convertDoubleToText(
                filterProductList.higherPrice
            )
        )
        binding.dateBetweenInput.setText(filterProductList.dateFrom + " - " + filterProductList.dateTo)
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private fun initObserver() {
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
        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it.map { it2 -> it2.name }
                ).also { adapter ->
                    binding.categoryNameInput.setAdapter(adapter)
                }
            }
        }
        receiptDataViewModel.filterProductList.observe(viewLifecycleOwner) {
            it?.let {
                filterProductList = it
                putFilterDefinitionIntoInputs()
            }
        }
    }
}