package com.example.navsample.fragments.saving

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.chart.ChartColors
import com.example.navsample.databinding.FragmentAddCategoryBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.entities.Category
import com.example.navsample.fragments.dialogs.ColorPickerDialog
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.ReceiptDataViewModel


class AddCategoryFragment : Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private var baseCategoryName: String = ""
    private var mode = DataMode.DISPLAY
    private var pickedColor: Int = ChartColors.DEFAULT_CATEGORY_COLOR_INT
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)


        binding.colorSquare.setBackgroundColor(pickedColor)
        receiptDataViewModel.refreshCategoryList()
        receiptDataViewModel.category.value?.let {
            baseCategoryName = it.name
            binding.categoryNameInput.setText(it.name)
            binding.categoryColorInput.setText(it.color)
            try {
                binding.colorSquare.setBackgroundColor(Color.parseColor(it.color))
            } catch (e: Exception) {
                Log.e(
                    "AddCategoryFragment",
                    "cannot parse category color" + it.color,
                )
            }
        }
        receiptDataViewModel.category.value = null
        if (receiptDataViewModel.savedCategory.value != null) {
            changeViewToDisplayMode()
        } else {
            mode = DataMode.NEW
            binding.categoryNameInput.setText("")

            binding.toolbar.title = "New category"
            binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
            binding.toolbar.setNavigationIcon(R.drawable.back)
            binding.toolbar.menu.findItem(R.id.edit).isVisible = false

        }


        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit -> {
                    changeViewToEditMode()
                    true
                }

                R.id.confirm -> {
                    if (binding.categoryColorLayout.error != null || binding.categoryNameLayout.error != null) {
                        Toast.makeText(
                            requireContext(),
                            "Incorrect input values",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnMenuItemClickListener true
                    }
                    saveChangesToDatabase()
                    changeViewToDisplayMode()
                    receiptDataViewModel.category.value = Category(
                        binding.categoryNameInput.text.toString(),
                        binding.categoryColorInput.text.toString()
                    )
                    listingViewModel.loadDataByCategoryFilter()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            if (mode == DataMode.EDIT) {
                changeViewToDisplayMode()
                binding.categoryNameInput.setText(receiptDataViewModel.savedCategory.value?.name)
                binding.categoryColorInput.setText(receiptDataViewModel.savedCategory.value?.color)
            } else {
                Navigation.findNavController(it).popBackStack()
            }
        }
        binding.colorView.setOnClickListener { _ ->
            colorPicker()
        }
        binding.categoryColorLayout.setStartIconOnClickListener {
            colorPicker()
        }
        binding.categoryColorInput.doOnTextChanged { text, _, _, count ->
            if (count == 7 && text != null && text[0] == '#') {
                try {
                    pickedColor = Color.parseColor(text.toString())
                    binding.colorSquare.setBackgroundColor(pickedColor)
                    binding.categoryColorLayout.error = null
                } catch (e: Exception) {
                    binding.categoryColorLayout.error = "Cannot parse"
                }
            } else {
                binding.categoryColorLayout.error = "Cannot parse"
            }
        }
        binding.categoryNameInput.doOnTextChanged { text, _, _, count ->
            if (count == 0) {
                binding.categoryNameLayout.error = "Cannot be empty"
            } else if (receiptDataViewModel.categoryList.value?.map { it.name }
                    ?.contains(text.toString()) == true && text.toString() != baseCategoryName) {
                binding.categoryNameLayout.error = "Category name already defined"
            } else {
                binding.categoryNameLayout.error = null
            }

        }
    }

    private fun colorPicker() {
        ColorPickerDialog(pickedColor) {
            pickedColor = it
            binding.colorSquare.setBackgroundColor(it)
            binding.categoryColorInput.setText(String.format("#%06X", 0xBBBBBB and it))
            binding.categoryColorLayout.error = null
        }
            .show(childFragmentManager, "TAG")
    }

    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            val category = Category(
                binding.categoryNameInput.text.toString(),
                binding.categoryColorInput.text.toString()
            )
            receiptDataViewModel.insertCategory(category)
        }
        if (mode == DataMode.EDIT) {
            val category = receiptDataViewModel.savedCategory.value!!
            category.name = binding.categoryNameInput.text.toString()
            category.color = binding.categoryColorInput.text.toString()
            receiptDataViewModel.updateCategory(category)
        }
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.categoryNameLayout.isEnabled = false
        binding.categoryColorLayout.isEnabled = false
        binding.colorView.isEnabled = false
        binding.toolbar.title = "Category"
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = true
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.toolbar.title = "Edit category"
        binding.categoryNameLayout.isEnabled = true
        binding.categoryColorLayout.isEnabled = true
        binding.colorView.isEnabled = true
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
        binding.toolbar.setNavigationIcon(R.drawable.clear)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false
    }
}