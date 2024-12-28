package com.example.navsample.fragments.saving

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.chart.ChartColors
import com.example.navsample.databinding.FragmentAddCategoryBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
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

    private var firstEntry = true
    private var stateAfterSave = false
    private var mode = DataMode.NEW
    private var pickedColor: Int = ChartColors.DEFAULT_CATEGORY_COLOR_INT
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun clearInputs() {
        addCategoryDataViewModel.categoryById.value = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true

        initObserver()
        binding.colorSquare.setBackgroundColor(pickedColor)
        addCategoryDataViewModel.refreshCategoryList()

        consumeNavArgs()
        applyInputParameters()


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!isCategoryInputValid()) {
                        return@setOnMenuItemClickListener false
                    }

                    stateAfterSave = true
                    saveChangesToDatabase()
                    true
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }
        binding.colorView.setOnClickListener { _ ->
            colorPicker()
        }
        binding.categoryColorLayout.setStartIconOnClickListener {
            colorPicker()
        }
        binding.categoryColorLayout.setEndIconOnClickListener {
            applyRandomColor()
        }
        binding.categoryColorInput.doOnTextChanged { text, _, _, count ->
            if (count == 7 && text != null && text[0] == '#') {
                try {
                    pickedColor = Color.parseColor(text.toString())
                    binding.colorSquare.setBackgroundColor(pickedColor)
                    binding.categoryColorLayout.error = null
                } catch (e: Exception) {
                    binding.categoryColorLayout.error = getString(R.string.invalid_color_format)
                }
            } else {
                binding.categoryColorLayout.error = getString(R.string.invalid_color_format)
            }
        }
        binding.categoryNameInput.doOnTextChanged { text, _, _, count ->
            if (count == 0) {
                binding.categoryNameLayout.error = getString(R.string.empty_value_error)
            } else if (text.toString() == addCategoryDataViewModel.categoryById.value?.name) {
                binding.categoryNameLayout.error = null
            } else if (addCategoryDataViewModel.categoryList.value?.find { it.name == text.toString() } != null) {
                binding.categoryNameLayout.error = getString(R.string.category_already_exists)
            } else {
                binding.categoryNameLayout.error = null
            }
        }
    }

    private fun consumeNavArgs() {
        if (firstEntry) {
            firstEntry = false
            addCategoryDataViewModel.inputType = navArgs.inputType
            addCategoryDataViewModel.categoryId = navArgs.categoryId
        }
    }


    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addCategoryDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addCategoryDataViewModel.categoryById.value = null
            mode = DataMode.NEW
            applyRandomColor()
            binding.categoryNameInput.setText("")
            binding.toolbar.title = getString(R.string.new_category_title)

        } else if (inputType == AddingInputType.ID) {
            if (addCategoryDataViewModel.categoryId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_category_title)
                mode = DataMode.EDIT
                addCategoryDataViewModel.getCategoryById(addCategoryDataViewModel.categoryId)
            } else {
                throw Exception("NO CATEGORY ID SET")
            }
        } else {
            throw Exception("BAD INPUT TYPE: " + addCategoryDataViewModel.inputType)
        }
    }

    private fun colorPicker() {
        ColorPickerDialog(pickedColor) { color ->
            applyColor(color)
            binding.categoryColorLayout.error = null
        }.show(childFragmentManager, "TAG")
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

    private fun isCategoryInputValid(): Boolean {
        if (binding.categoryColorLayout.error != null || binding.categoryNameLayout.error != null) {
            Toast.makeText(requireContext(), getString(R.string.bad_inputs), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun initObserver() {
        addCategoryDataViewModel.categoryById.observe(viewLifecycleOwner) {
            it?.let {
                binding.categoryNameInput.setText(it.name)
                binding.categoryColorInput.setText(it.color)
                binding.colorSquare.setBackgroundColor(ColorManager.parseColor(it.color))
            }
        }
        addCategoryDataViewModel.savedCategory.observe(viewLifecycleOwner) {
            it?.let {
                //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
                listingViewModel.loadDataByProductFilter()
                listingViewModel.loadDataByCategoryFilter()
                when (navArgs.sourceFragment) {
                    FragmentName.ADD_STORE_FRAGMENT -> {
                        val action =
                            AddCategoryFragmentDirections.actionAddCategoryFragmentToAddStoreFragment(
                                sourceFragment = FragmentName.ADD_CATEGORY_FRAGMENT,
                                categoryId = it.id,
                                storeId = "",
                                storeName = "",
                                storeNip = ""
                            )
                        Navigation.findNavController(requireView()).navigate(action)
                    }

                    FragmentName.ADD_PRODUCT_FRAGMENT -> {
                        val action =
                            AddCategoryFragmentDirections.actionAddCategoryFragmentToAddProductFragment(
                                sourceFragment = FragmentName.ADD_CATEGORY_FRAGMENT,
                                categoryId = it.id,
                                storeId = "",
                                receiptId = "",
                                productId = ""
                            )
                        Navigation.findNavController(requireView()).navigate(action)
                    }

                    else -> {
                        Navigation.findNavController(requireView()).popBackStack()
                    }
                }
                addCategoryDataViewModel.savedCategory.value = null
            }
        }
    }

    private fun generateRandomColor(): Int {
        val red = (0..255).random()
        val green = (0..255).random()
        val blue = (0..255).random()
        return Color.rgb(red, green, blue)
    }

    private fun applyRandomColor() {
        applyColor(generateRandomColor())
    }

    private fun applyColor(color: Int) {
        pickedColor = color
        binding.colorSquare.setBackgroundColor(pickedColor)
        binding.categoryColorInput.setText(intToColorString(pickedColor))
    }

    private fun intToColorString(colorInt: Int): String {
        val red = (colorInt shr 16) and 0xFF
        val green = (colorInt shr 8) and 0xFF
        val blue = colorInt and 0xFF
        return String.format("#%02X%02X%02X", red, green, blue)
    }
}