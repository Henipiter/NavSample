package com.example.navsample.fragments.saving

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.DTO.DataMode
import com.example.navsample.databinding.FragmentAddCategoryBinding
import com.example.navsample.entities.Category
import com.example.navsample.fragments.dialogs.ColorPickerDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel


class AddCategoryFragment : Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var mode = DataMode.DISPLAY
    private var pickedColor: Int = Color.rgb(255, 0, 0)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        receiptDataViewModel.savedCategory.value?.id = 11
        binding.colorSquare.setBackgroundColor(pickedColor)
        receiptDataViewModel.refreshCategoryList()
        receiptDataViewModel.category.value?.let {
            binding.categoryNameInput.setText(it.name)
            binding.categoryColorInput.setText(it.color)
            binding.colorSquare.setBackgroundColor(Color.parseColor(it.color))
        }
        receiptDataViewModel.category.value = null
        if (receiptDataViewModel.savedCategory.value != null) {
            changeViewToDisplayMode()
        } else {
            mode = DataMode.NEW
            binding.saveChangesButton.visibility = View.VISIBLE
            binding.cancelChangesButton.visibility = View.GONE
            binding.editButton.visibility = View.GONE
        }
        binding.editButton.setOnClickListener {
            changeViewToEditMode()
        }
        binding.saveChangesButton.setOnClickListener {

            saveChangesToDatabase()
            changeViewToDisplayMode()
            receiptDataViewModel.category.value = Category(
                binding.categoryNameInput.text.toString(),
                binding.categoryColorInput.text.toString()
            )
            receiptDataViewModel.refreshCategoryList()
            Navigation.findNavController(it).popBackStack()
        }
        binding.cancelChangesButton.setOnClickListener {
            changeViewToDisplayMode()
            binding.categoryNameInput.setText(receiptDataViewModel.savedCategory.value?.name)
            binding.categoryColorInput.setText(receiptDataViewModel.savedCategory.value?.color)
        }



        binding.colorView.setOnClickListener { v ->
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
                    binding.categoryColorLayout.error = "Cannnot parse"
                }
            } else {
                binding.categoryColorLayout.error = "Cannnot parse"
            }
        }
    }

    private fun colorPicker() {
        ColorPickerDialog(pickedColor) {
            pickedColor = it
            binding.colorSquare.setBackgroundColor(it)
            binding.categoryColorInput.setText(String.format("#%06X", 0xFFFFFF and it))
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
        binding.saveChangesButton.visibility = View.GONE
        binding.cancelChangesButton.visibility = View.GONE
        binding.editButton.visibility = View.VISIBLE
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.categoryNameLayout.isEnabled = true
        binding.categoryColorLayout.isEnabled = true
        binding.colorView.isEnabled = true
        binding.saveChangesButton.visibility = View.VISIBLE
        binding.cancelChangesButton.visibility = View.VISIBLE
        binding.editButton.visibility = View.GONE
    }
}