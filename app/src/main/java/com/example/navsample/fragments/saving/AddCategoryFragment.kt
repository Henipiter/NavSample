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
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.chart.ChartColors
import com.example.navsample.databinding.FragmentAddCategoryBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.fragments.dialogs.ColorPickerDialog
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddCategoryDataViewModel


class AddCategoryFragment : Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddCategoryFragmentArgs by navArgs()

    private val addCategoryDataViewModel: AddCategoryDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private var mode = DataMode.NEW
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
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true

        addCategoryDataViewModel.categoryById.value = null
        binding.colorSquare.setBackgroundColor(pickedColor)
        initObserver()
        addCategoryDataViewModel.refreshCategoryList()
        consumeNavArgs()

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
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
                    //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
                    listingViewModel.loadDataByProductFilter()
                    listingViewModel.loadDataByCategoryFilter()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
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
            } else if (text.toString() == addCategoryDataViewModel.categoryById.value?.name) {
                binding.categoryNameLayout.error = null
            } else if (addCategoryDataViewModel.categoryList.value?.find { it.name == text.toString() } != null) {
                binding.categoryNameLayout.error = "Category name already defined"
            } else {
                binding.categoryNameLayout.error = null
            }

        }
    }

    private fun consumeNavArgs() {
        val inputType = AddingInputType.getByName(navArgs.inputType)
        if (inputType == AddingInputType.EMPTY) {
            mode = DataMode.NEW
            binding.categoryNameInput.setText("")
            binding.toolbar.title = "New category"

        } else if (inputType == AddingInputType.ID) {
            if (navArgs.id >= 0) {
                binding.toolbar.title = "Edit category"
                mode = DataMode.EDIT
                addCategoryDataViewModel.getCategoryById(navArgs.id)
            } else {
                throw Exception("NO CATEGORY ID SET: " + navArgs.id)
            }
        } else {
            throw Exception("BAD INPUT TYPE: " + navArgs.inputType)
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
            addCategoryDataViewModel.insertCategory(category)
        }
        if (mode == DataMode.EDIT) {
            val category = addCategoryDataViewModel.categoryById.value!!
            category.name = binding.categoryNameInput.text.toString()
            category.color = binding.categoryColorInput.text.toString()
            addCategoryDataViewModel.updateCategory(category)
        }
    }

    private fun initObserver() {
        addCategoryDataViewModel.categoryById.observe(viewLifecycleOwner) {
            it?.let {
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
        }
    }
}