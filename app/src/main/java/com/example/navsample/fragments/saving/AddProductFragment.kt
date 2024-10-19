package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.adapters.PtuTypeDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.Utils.Companion.doubleToString
import com.example.navsample.dto.Utils.Companion.quantityToString
import com.example.navsample.dto.Utils.Companion.roundDouble
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddProductFragmentArgs by navArgs()

    private val imageViewModel: ImageViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels()

    private var mode = DataMode.NEW
    private var productOriginalInput = ""
    private var chosenCategory: Category? = null
    private var isValidPrices = true
    private lateinit var dropdownAdapter: CategoryDropdownAdapter

    companion object {
        private const val SUGGESTION_PREFIX = "Maybe "
        private const val EMPTY_VALUE_TEXT = "Empty"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun tryConvertToDouble(correctString: String): Double? {
        return correctString.toDoubleOrNull()

    }

    private fun getSuggestionMessage(value: String): String {
        return "${SUGGESTION_PREFIX}${value}"
    }

    private fun validateFinalPrice() {
        val subtotalPrice = tryConvertToDouble(binding.productSubtotalPriceInput.text.toString())
        val discountPrice = tryConvertToDouble(binding.productDiscountInput.text.toString())
        val finalPrice = tryConvertToDouble(binding.productFinalPriceInput.text.toString())
        val checks = arrayOf(discountPrice, finalPrice).count { it != null }
        if (subtotalPrice == -1.0) {
            return
        }
        if (checks == 2) {
            if (roundDouble(subtotalPrice!! - discountPrice!!) == finalPrice!!) {
                isValidPrices = true
                binding.productDiscountHelperText.text = ""
                binding.productFinalPriceHelperText.text = ""
            } else {
                isValidPrices = false
                binding.productDiscountHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice - finalPrice))
                binding.productFinalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice - discountPrice))
            }
        } else if (checks == 1) {
            isValidPrices = false
            if (discountPrice == null) {
                binding.productDiscountHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! - finalPrice!!))
            }
            if (finalPrice == null) {
                binding.productFinalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! - discountPrice!!))
            }
        } else {
            isValidPrices = true
            binding.productDiscountHelperText.text = ""
            binding.productFinalPriceHelperText.text = ""
        }
    }

    private fun validatePrices() {
        val subtotalPrice = tryConvertToDouble(binding.productSubtotalPriceInput.text.toString())
        val unitPrice = tryConvertToDouble(binding.productUnitPriceInput.text.toString())
        val quantity = tryConvertToDouble(binding.productQuantityInput.text.toString())

        val checks = arrayOf(subtotalPrice, unitPrice, quantity).count { it != null }
        if (checks == 3) {
            if (roundDouble(unitPrice!! * quantity!!) == subtotalPrice) {
                isValidPrices = true
                binding.productSubtotalPriceHelperText.text = ""
                binding.productUnitPriceHelperText.text = ""
                binding.productQuantityHelperText.text = ""
            } else {
                isValidPrices = false
                binding.productSubtotalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(unitPrice * quantity))
                binding.productUnitPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! / quantity))
                binding.productQuantityHelperText.text =
                    getSuggestionMessage(quantityToString(subtotalPrice / unitPrice))
            }
        } else if (checks == 2) {
            isValidPrices = false
            if (subtotalPrice == null) {
                binding.productSubtotalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(unitPrice!! * quantity!!))
            }

            if (unitPrice == null) {
                binding.productUnitPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! / quantity!!))
            }
            if (quantity == null) {
                binding.productQuantityHelperText.text =
                    getSuggestionMessage(quantityToString(subtotalPrice!! / unitPrice!!))
            }
        } else {
            isValidPrices = true
            binding.productSubtotalPriceHelperText.text = ""
            binding.productUnitPriceHelperText.text = ""
            binding.productQuantityHelperText.text = ""
        }
    }


    private fun validateObligatoryFields(): Boolean {
        var succeedValidation = true
        if (binding.productNameInput.text.isNullOrEmpty()) {
            binding.productNameLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.productSubtotalPriceInput.text.isNullOrEmpty()) {
            binding.productSubtotalPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productUnitPriceInput.text.isNullOrEmpty()) {
            binding.productUnitPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productQuantityInput.text.isNullOrEmpty()) {
            binding.productQuantityHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productDiscountInput.text.isNullOrEmpty()) {
            binding.productDiscountHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productFinalPriceInput.text.isNullOrEmpty()) {
            binding.productFinalPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        return succeedValidation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = false


        dropdownAdapter = CategoryDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.productCategoryInput.setAdapter(adapter)
        }
        PtuTypeDropdownAdapter(
            requireContext(), R.layout.array_adapter_row
        ).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }

        addProductDataViewModel.productById.value = null
        initObserver()
        addProductDataViewModel.refreshCategoryList()
        consumeNavArgs()


        addProductDataViewModel.categoryList.value?.let {
            if (chosenCategory == null) {
                val categoryId = addProductDataViewModel.storeById.value?.defaultCategoryId
                chosenCategory = try {
                    addProductDataViewModel.categoryList.value?.first { it.id == categoryId }
                } catch (e: Exception) {
                    Category("", "")
                }
            }
        }


        if (productOriginalInput == "") {
            binding.productOriginalLayout.visibility = View.INVISIBLE
        }
        binding.productCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            chosenCategory = adapter.getItemAtPosition(position) as Category

            if ("" == chosenCategory?.color && binding.productCategoryInput.adapter.count - 1 == position) {
                binding.productCategoryInput.setText("")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addProductFragment_to_addCategoryFragment)
            } else {
                binding.productCategoryInput.setText(chosenCategory?.name)
                binding.productCategoryInput.isEnabled = false
            }
        }
        binding.ptuTypeInput.setOnItemClickListener { adapter, _, i, _ ->
            val x = adapter.getItemAtPosition(i) as String
            binding.ptuTypeInput.setText(x)
        }

        binding.productCategoryLayout.setStartIconOnClickListener {
            binding.productCategoryInput.setText("")
            binding.productCategoryLayout.helperText = null
            binding.productCategoryInput.isEnabled = true
            chosenCategory = null
        }

        binding.productOriginalLayout.setEndIconOnClickListener {
            binding.productOriginalInput.setText(productOriginalInput)
        }
        binding.productNameInput.doOnTextChanged { actual, _, _, _ ->
            if (productOriginalInput != "" && !actual.isNullOrEmpty()) {
                binding.productNameLayout.error = null
                validatePrices()
            }
        }
        binding.productSubtotalPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productSubtotalPriceHelperText.text = ""
                validatePrices()
                validateFinalPrice()
            }
        }
        binding.productUnitPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productUnitPriceHelperText.text = ""
                validatePrices()
            }
        }
        binding.productQuantityInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productQuantityHelperText.text = ""
                validatePrices()
            }
        }
        binding.productDiscountInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productDiscountHelperText.text = ""
                validateFinalPrice()
            }
        }
        binding.productFinalPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productFinalPriceHelperText.text = ""
                validateFinalPrice()
            }
        }
        binding.productOriginalInput.doOnTextChanged { actual, _, _, _ ->
            val receiptParser = ReceiptParser(
                addProductDataViewModel.receiptById.value?.id ?: throw NoReceiptIdException(),
                addProductDataViewModel.storeById.value?.defaultCategoryId
                    ?: throw NoStoreIdException()
            )
            val product = receiptParser.parseStringToProduct(actual.toString())
            chosenCategory = try {
                addProductDataViewModel.categoryList.value?.first { it.id == product.categoryId }
            } catch (e: Exception) {
                null
            }
            binding.productNameInput.setText(product.name)
            binding.productSubtotalPriceInput.setText(product.subtotalPrice.toString())
            binding.productUnitPriceInput.setText(product.unitPrice.toString())
            binding.productQuantityInput.setText(product.quantity.toString())
            binding.ptuTypeInput.setText(product.ptuType)
            binding.productCategoryInput.setText(chosenCategory?.name)
            binding.productCategoryInput.isEnabled = false

        }
        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields()) {
                        return@setOnMenuItemClickListener false
                    }

                    saveChanges()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }

        binding.productSubtotalPriceHelperText.setOnClickListener {
            if (binding.productSubtotalPriceHelperText.text.toString()
                    .contains(SUGGESTION_PREFIX)
            ) {
                binding.productSubtotalPriceInput.setText(
                    convertSuggestionToValue(binding.productSubtotalPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productUnitPriceHelperText.setOnClickListener {
            if (binding.productUnitPriceHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productUnitPriceInput.setText(
                    convertSuggestionToValue(binding.productUnitPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productQuantityHelperText.setOnClickListener {
            if (binding.productQuantityHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productQuantityInput.setText(
                    convertSuggestionToValue(binding.productQuantityHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productDiscountHelperText.setOnClickListener {
            if (binding.productDiscountHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productDiscountInput.setText(
                    convertSuggestionToValue(binding.productDiscountHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productFinalPriceHelperText.setOnClickListener {
            if (binding.productFinalPriceHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productFinalPriceInput.setText(
                    convertSuggestionToValue(binding.productFinalPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
    }

    private fun consumeNavArgs() {
        when (AddingInputType.getByName(navArgs.inputType)) {
            AddingInputType.EMPTY -> {
                mode = DataMode.NEW
                binding.productNameInput.setText("")
                binding.productSubtotalPriceInput.setText("")
                binding.productUnitPriceInput.setText("")
                binding.productQuantityInput.setText("")
                binding.ptuTypeInput.setText("")
                binding.productCategoryInput.setText("")
                binding.toolbar.title = "Add product"
                if (addProductDataViewModel.storeById.value == null) {
                    isArgSetOrThrow(navArgs.storeId, "NO STORE ID SET: ")
                    { addProductDataViewModel.getStoreById(navArgs.storeId) }
                }
            }

            AddingInputType.INDEX -> {
                mode = DataMode.EDIT
                binding.toolbar.title = "Edit product"
                isArgSetOrThrow(navArgs.productIndex, "NO PRODUCT INDEX SET: ") {
                    addProductDataViewModel.productById.value =
                        addProductDataViewModel.productList.value?.get(navArgs.productIndex)
                }
            }

            AddingInputType.ID -> {
                mode = DataMode.EDIT
                binding.toolbar.title = "Edit product"
                isArgSetOrThrow(navArgs.productIndex, "NO PRODUCT ID  SET: ") {
                    addProductDataViewModel.getProductById(navArgs.productIndex)
                    if (addProductDataViewModel.storeById.value == null) {
                        isArgSetOrThrow(navArgs.storeId, "NO STORE ID SET: ")
                        { addProductDataViewModel.getStoreById(navArgs.storeId) }
                    }
                }
            }

            else -> {
                throw Exception("BAD INPUT TYPE: " + navArgs.inputType)
            }
        }
    }

    private fun isArgSetOrThrow(arg: Int, errorMessage: String, execute: () -> Unit) {
        if (arg >= 0) {
            execute.invoke()
        } else {
            throw Exception(errorMessage + arg)
        }
    }

    private fun saveChanges() {
        if (mode == DataMode.NEW) {
            val product = Product(
                addProductDataViewModel.receiptById.value?.id ?: throw NoReceiptIdException(),
                binding.productNameInput.text.toString(),
                chosenCategory?.id ?: throw NoCategoryIdException(),
                binding.productQuantityInput.text.toString().toDouble(),
                binding.productUnitPriceInput.text.toString().toDouble(),
                binding.productSubtotalPriceInput.text.toString().toDouble(),
                binding.productDiscountInput.text.toString().toDouble(),
                binding.productFinalPriceInput.text.toString().toDouble(),
                binding.ptuTypeInput.text.toString(),
                binding.productOriginalInput.text.toString(),
                isValidPrices
            )
            addProductDataViewModel.productList.value!!.add(product)


        } else if (mode == DataMode.EDIT) {
            val product = addProductDataViewModel.productById.value!!
            product.name = binding.productNameInput.text.toString()
            product.categoryId = chosenCategory?.id ?: throw NoCategoryIdException()
            product.quantity = binding.productQuantityInput.text.toString().toDouble()
            product.unitPrice = binding.productUnitPriceInput.text.toString().toDouble()
            product.subtotalPrice = binding.productSubtotalPriceInput.text.toString().toDouble()
            product.discount = binding.productDiscountInput.text.toString().toDouble()
            product.finalPrice = binding.productFinalPriceInput.text.toString().toDouble()
            product.ptuType = binding.ptuTypeInput.text.toString()
            product.raw = binding.productOriginalInput.text.toString()
            product.validPrice = isValidPrices

            when (navArgs.inputType) {
                AddingInputType.ID.name -> {
                    addProductDataViewModel.updateSingleProduct(product)
                }

                AddingInputType.INDEX.name -> {
                    addProductDataViewModel.productList.value!![navArgs.productIndex] = product
                }

                else -> {
                    throw Exception("PRODUCT INDEX IS NOT SET")
                }
            }
        }
    }

    private fun convertSuggestionToValue(suggestion: String): String {
        return suggestion.substring(
            SUGGESTION_PREFIX.length, suggestion.lastIndex + 1
        )
    }

    private fun initObserver() {
        imageViewModel.bitmapCroppedProduct.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(imageViewModel.bitmapCroppedProduct.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }
        addProductDataViewModel.categoryList.observe(viewLifecycleOwner) { categoryList ->
            categoryList?.let {
                dropdownAdapter.categoryList = it as ArrayList<Category>
                dropdownAdapter.notifyDataSetChanged()
            }
        }
        addProductDataViewModel.productById.observe(viewLifecycleOwner) { product ->
            product?.let {
                binding.productNameInput.setText(product.name)
                binding.productSubtotalPriceInput.setText(product.subtotalPrice.toString())
                binding.productUnitPriceInput.setText(product.unitPrice.toString())
                binding.productQuantityInput.setText(product.quantity.toString())
                binding.ptuTypeInput.setText(product.ptuType)
                val category =
                    addProductDataViewModel.categoryList.value?.first { it.id == product.categoryId }
                category?.let {
                    binding.productCategoryInput.setText(it.name)
                    binding.productCategoryInput.isEnabled = false
                }
            }
        }
        addProductDataViewModel.storeById.observe(viewLifecycleOwner) { store ->
            store?.let {
                binding.toolbar.title = it.name
            }
            val category =
                addProductDataViewModel.categoryList.value?.first { it.id == store?.defaultCategoryId }
            category?.let {
                binding.productCategoryInput.setText(it.name)
                binding.productCategoryInput.isEnabled = false
            }

        }
    }
}
