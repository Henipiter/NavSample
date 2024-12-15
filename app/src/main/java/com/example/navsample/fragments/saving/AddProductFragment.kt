package com.example.navsample.fragments.saving

import android.os.Bundle
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
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.adapters.PtuTypeDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.PriceUtils.Companion.doublePriceTextToInt
import com.example.navsample.dto.PriceUtils.Companion.doubleQuantityTextToInt
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.PriceUtils.Companion.intQuantityToString
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel
import kotlin.math.roundToInt

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddProductFragmentArgs by navArgs()

    private val imageViewModel: ImageViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels()

    private var mode = DataMode.NEW
    private var productOriginalInput = ""
    private var storeDefaultCategoryName = ""
    private var pickedCategory: Category? = null
    private var isValidPrices = true
    private var firstEntry = true
    private lateinit var dropdownAdapter: CategoryDropdownAdapter

    companion object {
        private const val SUGGESTION_PREFIX = "Maybe "
        private const val EMPTY_VALUE_TEXT = "Empty"
        private const val WRONG_VALUE_TEXT = "Wrong"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getCategorySuggestionMessage(): String {
        return "Use default category $storeDefaultCategoryName"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.importImage).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = false
        binding.toolbar.menu.findItem(R.id.aiParser).isVisible = false


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

        consumeNavArgs()
        initObserver()
        addProductDataViewModel.refreshCategoryList()
        applyInputParameters()

        if (productOriginalInput == "") {
            binding.productOriginalLayout.visibility = View.INVISIBLE
        }
        binding.productCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            pickedCategory = adapter.getItemAtPosition(position) as Category

            if ("" == pickedCategory?.color && binding.productCategoryInput.adapter.count - 1 == position) {
                binding.productCategoryInput.setText("")
                val action =
                    AddProductFragmentDirections.actionAddProductFragmentToAddCategoryFragment(
                        categoryId = "",
                        inputType = AddingInputType.EMPTY.name,
                        sourceFragment = FragmentName.ADD_PRODUCT_FRAGMENT
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                binding.productCategoryInput.setText(pickedCategory?.name)
                binding.productCategoryInput.isEnabled = false

                val storeCategory = addProductDataViewModel.storeById.value?.defaultCategoryId
                if (storeCategory != null && storeCategory != pickedCategory?.id) {
                    binding.productCategoryHelperText.text = getCategorySuggestionMessage()
                } else {
                    binding.productCategoryHelperText.text = ""
                }
            }
        }
        binding.ptuTypeInput.setOnItemClickListener { adapter, _, i, _ ->
            binding.ptuTypeInput.setText(adapter.getItemAtPosition(i) as String)
        }

        binding.productCategoryLayout.setStartIconOnClickListener {
            binding.productCategoryInput.setText("")
            binding.productCategoryLayout.helperText = null
            binding.productCategoryInput.isEnabled = true
            pickedCategory = null
            binding.productCategoryHelperText.text = getCategorySuggestionMessage()
        }

        binding.productOriginalLayout.setEndIconOnClickListener {
            binding.productOriginalInput.setText(productOriginalInput)
        }

        binding.productFinalPriceLayout.setEndIconOnClickListener {
            binding.productFinalPriceInput.setText("")
            validateFinalPrice()
        }
        binding.productDiscountLayout.setEndIconOnClickListener {
            binding.productDiscountInput.setText("")
            validateFinalPrice()
        }
        binding.productSubtotalPriceLayout.setEndIconOnClickListener {
            binding.productSubtotalPriceInput.setText("")
            validateFinalPrice()
            validatePrices()
        }
        binding.productUnitPriceLayout.setEndIconOnClickListener {
            binding.productUnitPriceInput.setText("")
            validatePrices()
        }
        binding.productQuantityLayout.setEndIconOnClickListener {
            binding.productQuantityInput.setText("")
            validatePrices()
        }

        binding.productNameInput.doOnTextChanged { actual, _, _, _ ->
            if (productOriginalInput != "" && !actual.isNullOrEmpty()) {
                binding.productNameLayout.error = null
            }
        }
        binding.productSubtotalPriceInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
            validateFinalPrice()
        }
        binding.productUnitPriceInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
        }
        binding.productQuantityInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
        }
        binding.productDiscountInput.doOnTextChanged { _, _, _, _ ->
            validateFinalPrice()
        }
        binding.productFinalPriceInput.doOnTextChanged { _, _, _, _ ->
            validateFinalPrice()
        }
        binding.productOriginalInput.doOnTextChanged { actual, _, _, _ ->
            if (addProductDataViewModel.receiptById.value == null || addProductDataViewModel.receiptById.value?.id?.isEmpty() == true) {
                throw NoReceiptIdException()
            }
            if (addProductDataViewModel.storeById.value == null || addProductDataViewModel.storeById.value?.defaultCategoryId?.isEmpty() == true) {
                throw NoStoreIdException()
            }

            val receiptParser = ReceiptParser(
                addProductDataViewModel.receiptById.value!!.id,
                addProductDataViewModel.storeById.value!!.defaultCategoryId
            )
            val product = receiptParser.parseStringToProduct(actual.toString())
            pickedCategory = try {
                addProductDataViewModel.categoryList.value?.first { it.id == product.categoryId }
            } catch (e: Exception) {
                null
            }
            binding.productNameInput.setText(product.name)
            binding.productSubtotalPriceInput.setText(product.subtotalPrice.toString())
            binding.productUnitPriceInput.setText(product.unitPrice.toString())
            binding.productQuantityInput.setText(product.quantity.toString())
            binding.productFinalPriceInput.setText(product.finalPrice.toString())
            binding.productDiscountInput.setText(product.discount.toString())

            binding.ptuTypeInput.setText(product.ptuType)
            binding.productCategoryInput.setText(pickedCategory?.name)
            binding.productCategoryInput.isEnabled = false

        }
        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields()) {
                        Toast.makeText(requireContext(), "Bad inputs", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener false
                    }

                    saveChanges()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }

        binding.productCategoryHelperText.setOnClickListener {
            if (binding.productCategoryHelperText.text != "") {
                val storeCategory = addProductDataViewModel.storeById.value?.defaultCategoryId
                if (storeCategory != null) {
                    addProductDataViewModel.categoryId = storeCategory
                    setCategory()
                    binding.productCategoryHelperText.text = ""
                }

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
        if (firstEntry && navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
            firstEntry = false
            addProductDataViewModel.inputType = navArgs.inputType
            addProductDataViewModel.productIndex = navArgs.productIndex
            addProductDataViewModel.productId = navArgs.productId
            addProductDataViewModel.receiptId = navArgs.receiptId
            addProductDataViewModel.storeId = navArgs.storeId
            addProductDataViewModel.categoryId = navArgs.categoryId
        } else if (navArgs.sourceFragment == FragmentName.ADD_CATEGORY_FRAGMENT) {
            if (navArgs.categoryId.isNotEmpty()) {
                addProductDataViewModel.categoryId = navArgs.categoryId
            }
        }
    }

    private fun applyInputParameters() {
        when (AddingInputType.getByName(addProductDataViewModel.inputType)) {
            AddingInputType.EMPTY -> {
                addProductDataViewModel.productById.value = null
                mode = DataMode.NEW
                binding.productNameInput.setText("")
                binding.productSubtotalPriceInput.setText("")
                binding.productUnitPriceInput.setText("")
                binding.productQuantityInput.setText("")
                binding.productFinalPriceInput.setText("")
                binding.productDiscountInput.setText("")
                binding.ptuTypeInput.setText("")
                binding.productCategoryInput.setText("")
                binding.toolbar.title = "Add product"
                binding.productQuantityHelperText.text = getSuggestionMessage("1")
                binding.productDiscountHelperText.text = getSuggestionMessage("0.00")
                if (addProductDataViewModel.storeById.value == null) {
                    isArgSetOrThrow(addProductDataViewModel.storeId, "NO STORE ID SET: ")
                    { addProductDataViewModel.getStoreById(addProductDataViewModel.storeId) }
                }
            }

            AddingInputType.INDEX -> {
                mode = DataMode.EDIT
                binding.toolbar.title = "Edit product"
                isArgSetOrThrow(addProductDataViewModel.productIndex, "NO PRODUCT INDEX SET: ") {
                    addProductDataViewModel.productById.value =
                        addProductDataViewModel.productList.value?.get(addProductDataViewModel.productIndex)
                }
            }

            AddingInputType.ID -> {
                mode = DataMode.EDIT
                binding.toolbar.title = "Edit product"
                isArgSetOrThrow(addProductDataViewModel.productId, "NO PRODUCT ID  SET: ") {
                    addProductDataViewModel.getProductById(addProductDataViewModel.productId)
                    if (addProductDataViewModel.storeById.value == null) {
                        isArgSetOrThrow(addProductDataViewModel.storeId, "NO STORE ID SET: ")
                        { addProductDataViewModel.getStoreById(addProductDataViewModel.storeId) }
                    }
                }
            }

            else -> {
                throw Exception("BAD INPUT TYPE: " + addProductDataViewModel.inputType)
            }
        }
    }

    private fun saveChanges() {
        if (mode == DataMode.NEW) {
            val product = Product(
                addProductDataViewModel.receiptId.ifEmpty { throw NoReceiptIdException() },
                binding.productNameInput.text.toString(),
                pickedCategory?.id ?: throw NoCategoryIdException(),
                doubleQuantityTextToInt(binding.productQuantityInput.text.toString()),
                doublePriceTextToInt(binding.productUnitPriceInput.text.toString()),
                doublePriceTextToInt(binding.productSubtotalPriceInput.text.toString()),
                doublePriceTextToInt(binding.productDiscountInput.text.toString()),
                doublePriceTextToInt(binding.productFinalPriceInput.text.toString()),
                binding.ptuTypeInput.text.toString(),
                binding.productOriginalInput.text.toString(),
                isValidPrices
            )
            val newList =
                addProductDataViewModel.productList.value?.let { ArrayList(it) } ?: arrayListOf()
            newList.add(product)
            addProductDataViewModel.productList.value = newList


        } else if (mode == DataMode.EDIT) {
            val product = addProductDataViewModel.productById.value!!
            product.name = binding.productNameInput.text.toString()
            product.categoryId = pickedCategory?.id ?: throw NoCategoryIdException()
            product.quantity = doubleQuantityTextToInt(binding.productQuantityInput.text.toString())
            product.unitPrice = doublePriceTextToInt(binding.productUnitPriceInput.text.toString())
            product.subtotalPrice =
                doublePriceTextToInt(binding.productSubtotalPriceInput.text.toString())
            product.discount = doublePriceTextToInt(binding.productDiscountInput.text.toString())
            product.finalPrice =
                doublePriceTextToInt(binding.productFinalPriceInput.text.toString())
            product.ptuType = binding.ptuTypeInput.text.toString()
            product.raw = binding.productOriginalInput.text.toString()
            product.validPrice = isValidPrices

            when (addProductDataViewModel.inputType) {
                AddingInputType.ID.name -> {
                    addProductDataViewModel.updateSingleProduct(product)
                }

                AddingInputType.INDEX.name -> {
                    addProductDataViewModel.productList.value!![addProductDataViewModel.productIndex] =
                        product
                    listingViewModel.loadDataByCategoryFilter()
                }

                else -> {
                    throw Exception("PRODUCT INDEX IS NOT SET")
                }
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

    private fun isArgSetOrThrow(arg: String?, errorMessage: String, execute: () -> Unit) {
        if (arg != "") {
            execute.invoke()
        } else {
            throw Exception(errorMessage)
        }
    }

    private fun getSuggestionMessage(value: String): String {
        return "${SUGGESTION_PREFIX}${value}"
    }

    private fun validateFinalPrice() {
        val isSubtotalPriceBlank =
            binding.productSubtotalPriceInput.text.isBlank() || binding.productSubtotalPriceInput.text.toString()
                .toDouble() == 0.0
        val isDiscountPriceBlank = binding.productDiscountInput.text.isBlank()
        val isFinalPriceBlank =
            binding.productFinalPriceInput.text.isBlank() || binding.productFinalPriceInput.text.toString()
                .toDouble() == 0.0

        var subtotalPrice = 1
        var discountPrice = 0
        var finalPrice = 1
        if (!isSubtotalPriceBlank) {
            subtotalPrice = doublePriceTextToInt(binding.productSubtotalPriceInput.text.toString())
        }
        if (!isDiscountPriceBlank) {
            discountPrice = doublePriceTextToInt(binding.productDiscountInput.text.toString())
        }
        if (!isFinalPriceBlank) {
            finalPrice = doublePriceTextToInt(binding.productFinalPriceInput.text.toString())
        }

        if (!isSubtotalPriceBlank && !isDiscountPriceBlank && !isFinalPriceBlank) {
            if (subtotalPrice - discountPrice == finalPrice) {
                isValidPrices = true
                binding.productDiscountHelperText.text = ""
                binding.productFinalPriceHelperText.text = ""
            } else {
                isValidPrices = false
                binding.productDiscountHelperText.text =
                    getSuggestionMessage(intPriceToString(subtotalPrice - finalPrice))
                binding.productFinalPriceHelperText.text =
                    getSuggestionMessage(intPriceToString(subtotalPrice - discountPrice))
            }
        } else if (!isSubtotalPriceBlank && isDiscountPriceBlank && !isFinalPriceBlank) {
            isValidPrices = false
            binding.productDiscountHelperText.text =
                getSuggestionMessage(intPriceToString(subtotalPrice - finalPrice))
        } else if (!isSubtotalPriceBlank && !isDiscountPriceBlank) {
            isValidPrices = false
            binding.productFinalPriceHelperText.text =
                getSuggestionMessage(intPriceToString(subtotalPrice - discountPrice))
        } else if (isDiscountPriceBlank) {
            isValidPrices = false
            binding.productDiscountHelperText.text = getSuggestionMessage("0.00")
        } else {
            isValidPrices = true
            binding.productDiscountHelperText.text = ""
            binding.productFinalPriceHelperText.text = ""
        }
    }

    private fun validatePrices() {

        binding.productSubtotalPriceHelperText.text = ""
        binding.productUnitPriceHelperText.text = ""
        binding.productQuantityHelperText.text = ""

        val isSubtotalPriceBlank =
            binding.productSubtotalPriceInput.text.isBlank() || binding.productSubtotalPriceInput.text.toString()
                .toDouble() == 0.0
        val isUnitPriceBlank =
            binding.productUnitPriceInput.text.isBlank() || binding.productUnitPriceInput.text.toString()
                .toDouble() == 0.0
        val isQuantityBlank =
            binding.productQuantityInput.text.isBlank() || binding.productQuantityInput.text.toString()
                .toDouble() == 0.0

        var subtotalPrice = 1
        var unitPrice = 1
        var quantity = 1
        if (!isSubtotalPriceBlank) {
            subtotalPrice =
                doublePriceTextToInt(binding.productSubtotalPriceInput.text.toString()) * 1000
        }
        if (!isUnitPriceBlank) {
            unitPrice = doublePriceTextToInt(binding.productUnitPriceInput.text.toString())
        }
        if (!isQuantityBlank) {
            quantity = doubleQuantityTextToInt(binding.productQuantityInput.text.toString())
        }
        val checks = arrayOf(isSubtotalPriceBlank, isUnitPriceBlank, isQuantityBlank).count { !it }
        if (checks == 3) {
            if (roundInt(unitPrice * quantity) * 1000 == subtotalPrice) {
                isValidPrices = true
                binding.productSubtotalPriceHelperText.text = ""
                binding.productUnitPriceHelperText.text = ""
                binding.productQuantityHelperText.text = ""
            } else {
                isValidPrices = false
                binding.productSubtotalPriceHelperText.text =
                    getSuggestionMessage(intPriceToString(roundInt(unitPrice * quantity)))

                val unitPriceMessage =
                    if (quantity != 0) intPriceToString(subtotalPrice / quantity) else "100"
                binding.productUnitPriceHelperText.text = getSuggestionMessage(unitPriceMessage)

                val unitQuantityMessage =
                    if (quantity != 0) intQuantityToString(subtotalPrice / unitPrice) else "1000"
                binding.productQuantityHelperText.text = getSuggestionMessage(unitQuantityMessage)
            }
        } else if (checks == 2) {
            isValidPrices = false
            if (isSubtotalPriceBlank) {
                binding.productSubtotalPriceHelperText.text =
                    getSuggestionMessage(intPriceToString(roundInt(unitPrice * quantity)))
            }
            if (isUnitPriceBlank) {
                val unitPriceMessage =
                    if (quantity != 0) intPriceToString(subtotalPrice / quantity) else "100"
                binding.productUnitPriceHelperText.text = getSuggestionMessage(unitPriceMessage)
            }
            if (isQuantityBlank) {
                val unitQuantityMessage =
                    if (unitPrice != 0) intQuantityToString(subtotalPrice / unitPrice) else "1000"
                binding.productQuantityHelperText.text = getSuggestionMessage(unitQuantityMessage)
            }
        } else {
            isValidPrices = false
            binding.productSubtotalPriceHelperText.text = ""
            binding.productUnitPriceHelperText.text = ""
            binding.productQuantityHelperText.text = ""
        }
        if (isQuantityBlank && checks <= 1) {
            binding.productQuantityHelperText.text = getSuggestionMessage("1")
        } else if (!isQuantityBlank && checks <= 1) {
            binding.productQuantityHelperText.text = ""

        }
    }

    private fun roundInt(integer: Int): Int {
        return (integer / 1000.0).roundToInt()
    }

    private fun validateObligatoryFields(): Boolean {
        var succeedValidation = true
        if (binding.productNameInput.text.isNullOrEmpty()) {
            binding.productNameLayout.error = EMPTY_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productSubtotalPriceInput.text.isNullOrEmpty()) {
            binding.productSubtotalPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        } else if (binding.productSubtotalPriceInput.text.toString().toDouble() <= 0.0) {
            binding.productSubtotalPriceHelperText.text = WRONG_VALUE_TEXT
            binding.productNameLayout.error = WRONG_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productUnitPriceInput.text.isNullOrEmpty()) {
            binding.productUnitPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        } else if (binding.productUnitPriceInput.text.toString().toDouble() <= 0.0) {
            binding.productUnitPriceHelperText.text = WRONG_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productQuantityInput.text.isNullOrEmpty()) {
            binding.productQuantityHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        } else if (binding.productQuantityInput.text.toString().toDouble() <= 0.0) {
            binding.productQuantityHelperText.text = WRONG_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productDiscountInput.text.isNullOrEmpty()) {
            binding.productDiscountHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        } else if (binding.productDiscountInput.text.toString().toDouble() < 0.0) {
            binding.productDiscountHelperText.text = WRONG_VALUE_TEXT
            succeedValidation = false
        }
        if (binding.productFinalPriceInput.text.isNullOrEmpty()) {
            binding.productFinalPriceHelperText.text = EMPTY_VALUE_TEXT
            succeedValidation = false
        } else if (binding.productFinalPriceInput.text.toString().toDouble() <= 0.0) {
            binding.productFinalPriceHelperText.text = WRONG_VALUE_TEXT
            succeedValidation = false
        }
        if (!isValidPrices) {
            succeedValidation = false
        }
        return succeedValidation
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

                if (addProductDataViewModel.categoryId.isNotEmpty()) {
                    setCategory()
                }
            }
            val storeCategoryId = addProductDataViewModel.storeById.value?.defaultCategoryId
            if (storeCategoryId != null) {
                categoryList.first { category -> category.id == storeCategoryId }.name.let { name ->
                    name.let { storeDefaultCategoryName = it }
                }
            }

        }
        addProductDataViewModel.productById.observe(viewLifecycleOwner) {
            it?.let { product ->
                if (addProductDataViewModel.receiptById.value == null) {
                    addProductDataViewModel.getReceiptById(product.receiptId)
                }
                if (navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
                    addProductDataViewModel.categoryId = it.categoryId
                    setCategory()
                }

                binding.productNameInput.setText(product.name)
                binding.productSubtotalPriceInput.setText(intPriceToString(product.subtotalPrice))
                binding.productUnitPriceInput.setText(intPriceToString(product.unitPrice))
                binding.productQuantityInput.setText(intQuantityToString(product.quantity))
                binding.productFinalPriceInput.setText(intPriceToString(product.finalPrice))
                binding.productDiscountInput.setText(intPriceToString(product.discount))
                binding.ptuTypeInput.setText(product.ptuType)
            }
        }
        addProductDataViewModel.storeById.observe(viewLifecycleOwner) {
            it?.let { store ->
                binding.toolbar.title = store.name
                if (addProductDataViewModel.receiptById.value == null && addProductDataViewModel.categoryId == "") {
                    addProductDataViewModel.categoryId = store.defaultCategoryId
                    setCategory()
                }
                addProductDataViewModel.categoryList.value?.first { category -> category.id == store.defaultCategoryId }?.name.let { name ->
                    name?.let { storeDefaultCategoryName = it }
                }
            }
        }
    }

    private fun setCategory() {
        pickedCategory = try {
            addProductDataViewModel.categoryList.value?.first { category -> category.id == addProductDataViewModel.categoryId }
        } catch (exception: Exception) {
            null
        }
        pickedCategory?.let {
            binding.productCategoryInput.setText(it.name)
            binding.productCategoryInput.isEnabled = false
        }
    }
}
