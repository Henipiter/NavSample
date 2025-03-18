package com.example.navsample.fragments.saving

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
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
import com.example.navsample.entities.inputs.CategoryInputs
import com.example.navsample.fragments.dialogs.ColorPickerDialog
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddCategoryDataViewModel


class AddCategoryFragment : AddingFragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddCategoryFragmentArgs by navArgs()

    private val addCategoryDataViewModel: AddCategoryDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private var firstEntry = true
    private var stateAfterSave = false
    private var pickedColor: Int = ChartColors.DEFAULT_CATEGORY_COLOR_INT
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields(getInputs())) {
                        return@setOnMenuItemClickListener false
                    }
                    stateAfterSave = true
                    save()
                    true
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineToolbar()
        initObserver()

        applyColor(ChartColors.DEFAULT_CATEGORY_COLOR_INT)
        addCategoryDataViewModel.refreshCategoryList()

        consumeNavArgs()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )

        binding.colorView.setOnClickListener { _ ->
            colorPicker()
        }
        binding.categoryColorLayout.setStartIconOnClickListener {
            colorPicker()
        }
        binding.categoryColorLayout.setEndIconOnClickListener {
            applyRandomColor()
        }
        binding.categoryColorInput.doOnTextChanged { text, _, _, _ ->
            val errorMessage = addCategoryDataViewModel.validateColor(text)
            binding.categoryColorLayout.error = errorMessage
            binding.categoryColorLayout.errorIconDrawable = null
        }
        binding.categoryNameInput.doOnTextChanged { text, _, _, _ ->
            binding.categoryNameLayout.error = addCategoryDataViewModel.validateName(text)
        }
    }

    override fun consumeNavArgs() {
        if (firstEntry) {
            firstEntry = false
            addCategoryDataViewModel.inputType = navArgs.inputType
            addCategoryDataViewModel.categoryId = navArgs.categoryId
        }
        applyInputParameters()
    }

    override fun clearInputs() {
        addCategoryDataViewModel.categoryById.value = null
    }

    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addCategoryDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addCategoryDataViewModel.categoryById.value = null
            addCategoryDataViewModel.mode = DataMode.NEW
            applyRandomColor()
            binding.categoryNameInput.setText("")
            binding.toolbar.title = getString(R.string.new_category_title)

        } else if (inputType == AddingInputType.ID) {
            if (addCategoryDataViewModel.categoryId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_category_title)
                addCategoryDataViewModel.mode = DataMode.EDIT
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

    private fun validateObligatoryFields(categoryInputs: CategoryInputs): Boolean {
        val errors = addCategoryDataViewModel.validateObligatoryFields(categoryInputs)
        binding.categoryNameLayout.error = errors.name
        binding.categoryColorLayout.error = errors.color
        binding.categoryColorLayout.errorIconDrawable = null
        return errors.isCorrect()
    }

    override fun save() {
        addCategoryDataViewModel.saveCategory(getInputs())
    }

    private fun getInputs(): CategoryInputs {
        return CategoryInputs(
            binding.categoryNameInput.text.toString(),
            binding.categoryColorInput.text.toString()
        )
    }

    override fun initObserver() {
        addCategoryDataViewModel.categoryById.observe(viewLifecycleOwner) {
            it?.let {
                binding.categoryNameInput.setText(it.name)
                applyColor(it.color)

                validateObligatoryFields(getInputs())
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
                                tagId = "",
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

    private fun applyColor(color: String) {
        pickedColor = ColorManager.parseColor(color)
        binding.colorSquare.setBackgroundColor(pickedColor)
        binding.categoryColorInput.setText(color)
    }

    private fun intToColorString(colorInt: Int): String {
        val red = (colorInt shr 16) and 0xFF
        val green = (colorInt shr 8) and 0xFF
        val blue = colorInt and 0xFF
        return String.format("#%02X%02X%02X", red, green, blue)
    }
}