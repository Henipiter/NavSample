package com.example.navsample.fragments.saving

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
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.adapters.PtuTypeDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.PriceUtils.Companion.doublePriceTextToInt
import com.example.navsample.dto.PriceUtils.Companion.doubleQuantityTextToInt
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.PriceUtils.Companion.intQuantityToString
import com.example.navsample.dto.TagList
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.inputs.ProductInputs
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.factory.AddProductDataViewModelFactory
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.runBlocking


class AddProductFragment : AddingFragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddProductFragmentArgs by navArgs()

    private val imageViewModel: ImageViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels {
        AddProductDataViewModelFactory(requireActivity().application)
    }

    private var isValidPrices = true
    private var firstEntry = true
    private lateinit var dropdownAdapter: CategoryDropdownAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.importImage).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = false
        binding.toolbar.menu.findItem(R.id.aiAssistant).isVisible = false

        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields()) {
                        ConfirmDialog(
                            "Invalid prices",
                            "Some products have invalid prices. Continue?"
                        )
                        {
                            isValidPrices = false
                            save()
                            Navigation.findNavController(requireView()).popBackStack()
                        }.show(childFragmentManager, "TAG")
                    } else {
                        isValidPrices = true
                        save()
                        Navigation.findNavController(requireView()).popBackStack()
                    }
                    true
                }

                else -> false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineToolbar()


        dropdownAdapter = CategoryDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, listOf()
        ).also { adapter ->
            binding.productCategoryInput.setAdapter(adapter)
        }
        PtuTypeDropdownAdapter(requireContext(), R.layout.array_adapter_row).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }

        consumeNavArgs()
        initObserver()
        addProductDataViewModel.refreshCategoryList()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )

        binding.productCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            addProductDataViewModel.pickedCategory = adapter.getItemAtPosition(position) as Category

            if ("" == addProductDataViewModel.pickedCategory?.color && binding.productCategoryInput.adapter.count - 1 == position) {
                binding.productCategoryInput.setText("")
                saveInputsToViewModel()
                val action =
                    AddProductFragmentDirections.actionAddProductFragmentToAddCategoryFragment(
                        categoryId = "",
                        inputType = AddingInputType.EMPTY.name,
                        sourceFragment = FragmentName.ADD_PRODUCT_FRAGMENT
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                binding.productCategoryInput.setText(addProductDataViewModel.pickedCategory?.name)
                validateCategory()
            }
        }
        binding.ptuTypeInput.setOnItemClickListener { adapter, _, i, _ ->
            binding.ptuTypeInput.setText(adapter.getItemAtPosition(i) as String)

            val error = addProductDataViewModel.validatePtuType(binding.ptuTypeInput.text)
            binding.ptuTypeLayout.error = error
        }

        binding.productCategoryLayout.setStartIconOnClickListener {
            binding.productCategoryInput.setText("")
            addProductDataViewModel.pickedCategory = null
            validateCategory()
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
            val error = addProductDataViewModel.validateName(actual)
            binding.productNameLayout.error = error
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

        binding.productCategoryHelperText.setOnClickListener {
            if (binding.productCategoryHelperText.text != "") {
                val storeCategory = addProductDataViewModel.storeById.value?.defaultCategoryId
                if (storeCategory != null) {
                    addProductDataViewModel.categoryId = storeCategory
                    setCategory()
                }

            }
        }
        binding.productSubtotalPriceHelperText.setOnClickListener {
            if (binding.productSubtotalPriceHelperText.text.toString()
                    .contains(addProductDataViewModel.getSuggestionPrefix())
            ) {
                binding.productSubtotalPriceInput.setText(
                    convertSuggestionToValue(binding.productSubtotalPriceHelperText.text.toString())
                )
                validatePrices()
                validateFinalPrice()
            }
        }
        binding.productSubtotalPriceSecondHelperText.setOnClickListener {
            if (binding.productSubtotalPriceSecondHelperText.text.toString()
                    .contains(addProductDataViewModel.getSecondSuggestionPrefix())
            ) {
                binding.productSubtotalPriceInput.setText(
                    convertSecondSuggestionToValue(binding.productSubtotalPriceSecondHelperText.text.toString())
                )
                validatePrices()
                validateFinalPrice()
            }
        }
        binding.productUnitPriceHelperText.setOnClickListener {
            if (binding.productUnitPriceHelperText.text.toString()
                    .contains(addProductDataViewModel.getSuggestionPrefix())
            ) {
                binding.productUnitPriceInput.setText(
                    convertSuggestionToValue(binding.productUnitPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productQuantityHelperText.setOnClickListener {
            if (binding.productQuantityHelperText.text.toString()
                    .contains(addProductDataViewModel.getSuggestionPrefix())
            ) {
                binding.productQuantityInput.setText(
                    convertSuggestionToValue(binding.productQuantityHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productDiscountHelperText.setOnClickListener {
            if (binding.productDiscountHelperText.text.toString()
                    .contains(addProductDataViewModel.getSuggestionPrefix())
            ) {
                binding.productDiscountInput.setText(
                    convertSuggestionToValue(binding.productDiscountHelperText.text.toString())
                )
                validateFinalPrice()
            }
        }
        binding.productFinalPriceHelperText.setOnClickListener {
            if (binding.productFinalPriceHelperText.text.toString()
                    .contains(addProductDataViewModel.getSuggestionPrefix())
            ) {
                binding.productFinalPriceInput.setText(
                    convertSuggestionToValue(binding.productFinalPriceHelperText.text.toString())
                )
                validateFinalPrice()
            }
        }
    }

    private fun saveInputsToViewModel() {
        addProductDataViewModel.productInputs.name = binding.productNameInput.text.toString()
        addProductDataViewModel.productInputs.quantity =
            doubleQuantityTextToInt(binding.productQuantityInput.text)
        addProductDataViewModel.productInputs.unitPrice =
            doublePriceTextToInt(binding.productUnitPriceInput.text)
        addProductDataViewModel.productInputs.subtotalPrice =
            doublePriceTextToInt(binding.productSubtotalPriceInput.text)
        addProductDataViewModel.productInputs.discount =
            doublePriceTextToInt(binding.productDiscountInput.text)
        addProductDataViewModel.productInputs.finalPrice =
            doublePriceTextToInt(binding.productFinalPriceInput.text)
        addProductDataViewModel.productInputs.ptuType = binding.ptuTypeInput.text.toString()

        addProductDataViewModel.productInputs.tagList = binding.chipGroup.getTagIds()
    }

    private fun putInputsFromViewModel() {
        binding.productNameInput.setText(addProductDataViewModel.productInputs.name)
        binding.productSubtotalPriceInput.setText(intPriceToString(addProductDataViewModel.productInputs.subtotalPrice))
        binding.productUnitPriceInput.setText(intPriceToString(addProductDataViewModel.productInputs.unitPrice))
        binding.productQuantityInput.setText(intQuantityToString(addProductDataViewModel.productInputs.quantity))
        binding.productFinalPriceInput.setText(intPriceToString(addProductDataViewModel.productInputs.finalPrice))
        binding.productDiscountInput.setText(intPriceToString(addProductDataViewModel.productInputs.discount))
        binding.ptuTypeInput.setText(addProductDataViewModel.productInputs.ptuType)

        binding.chipGroup.removeAllViews()
        addProductDataViewModel.refreshTagsList(addProductDataViewModel.productInputs.tagList)
    }

    override fun consumeNavArgs() {
        if (firstEntry && navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
            firstEntry = false
            addProductDataViewModel.inputType = navArgs.inputType
            addProductDataViewModel.productIndex = navArgs.productIndex
            addProductDataViewModel.productId = navArgs.productId
            addProductDataViewModel.receiptId = navArgs.receiptId
            addProductDataViewModel.storeId = navArgs.storeId
            if (navArgs.categoryId.isNotEmpty()) {
                addProductDataViewModel.categoryId = navArgs.categoryId
            }
            applyInputParameters()
            addProductDataViewModel.refreshTagsList()
        } else if (navArgs.sourceFragment == FragmentName.ADD_CATEGORY_FRAGMENT) {
            putInputsFromViewModel()
            if (navArgs.categoryId.isNotEmpty()) {
                addProductDataViewModel.categoryId = navArgs.categoryId
            }
        }
    }

    private fun applyInputParameters() {
        when (AddingInputType.getByName(addProductDataViewModel.inputType)) {
            AddingInputType.EMPTY -> {
                addProductDataViewModel.productById.value = null
                addProductDataViewModel.mode = DataMode.NEW
                binding.productNameInput.setText("")
                binding.productSubtotalPriceInput.setText("")
                binding.productUnitPriceInput.setText("")
                binding.productQuantityInput.setText("")
                binding.productFinalPriceInput.setText("")
                binding.productDiscountInput.setText("")
                binding.ptuTypeInput.setText("")
                binding.productCategoryInput.setText("")
                binding.toolbar.title = getString(R.string.new_product_title)
                binding.productQuantityHelperText.text =
                    addProductDataViewModel.getSuggestionMessage("1")
                binding.productDiscountHelperText.text =
                    addProductDataViewModel.getSuggestionMessage("0.00")
                if (addProductDataViewModel.storeById.value == null) {
                    isArgSetOrThrow(addProductDataViewModel.storeId, "NO STORE ID SET: ")

                    addProductDataViewModel.getStoreById(addProductDataViewModel.storeId)
                }
                validateObligatoryFields()
            }

            AddingInputType.INDEX -> {
                addProductDataViewModel.mode = DataMode.EDIT
                binding.toolbar.title = getString(R.string.edit_product_title)
                isProductIndexSet()

                val product =
                    addProductDataViewModel.aggregatedProductList.value?.get(addProductDataViewModel.productIndex)
                addProductDataViewModel.productId = product?.id ?: ""
                addProductDataViewModel.productById.postValue(product)

            }

            AddingInputType.ID -> {
                addProductDataViewModel.mode = DataMode.EDIT
                binding.toolbar.title = getString(R.string.edit_product_title)
                isArgSetOrThrow(addProductDataViewModel.productId, "NO PRODUCT ID  SET: ")
                addProductDataViewModel.getProductById(addProductDataViewModel.productId)
                if (addProductDataViewModel.storeById.value == null) {
                    isArgSetOrThrow(addProductDataViewModel.storeId, "NO STORE ID SET: ")
                    addProductDataViewModel.getStoreById(addProductDataViewModel.storeId)
                }

            }

            else -> {
                throw Exception("BAD INPUT TYPE: " + addProductDataViewModel.inputType)
            }
        }
    }

    private fun ChipGroup.getTagIds(): List<Tag> {
        val ids = mutableListOf<Tag>()
        for (i in 0 until childCount) {
            val chip = getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                chip.tag?.let { tag ->
                    if (tag is Tag) {
                        ids.add(tag)
                    }
                }
            }
        }
        return ids
    }

    private fun getInputs(): ProductInputs {
        return ProductInputs(
            binding.productNameInput.text,
            binding.productCategoryInput.text,
            binding.productQuantityInput.text,
            binding.productUnitPriceInput.text,
            binding.productSubtotalPriceInput.text,
            binding.productDiscountInput.text,
            binding.productFinalPriceInput.text,
            binding.ptuTypeInput.text
        )
    }

    override fun save() {
        if (addProductDataViewModel.mode == DataMode.NEW) {
            val product = Product(
                addProductDataViewModel.receiptId.ifEmpty { throw NoReceiptIdException() },
                binding.productNameInput.text.toString(),
                addProductDataViewModel.pickedCategory?.id ?: throw NoCategoryIdException(),
                doubleQuantityTextToInt(binding.productQuantityInput.text),
                doublePriceTextToInt(binding.productUnitPriceInput.text),
                doublePriceTextToInt(binding.productSubtotalPriceInput.text),
                doublePriceTextToInt(binding.productDiscountInput.text),
                doublePriceTextToInt(binding.productFinalPriceInput.text),
                binding.ptuTypeInput.text.toString(),
                "",
                isValidPrices
            )
            product.tagList = binding.chipGroup.getTagIds()
            val newList =
                addProductDataViewModel.temporaryProductList.value?.let { ArrayList(it) }
                    ?: arrayListOf()
            newList.add(product)
            addProductDataViewModel.temporaryProductList.postValue(newList)


        } else if (addProductDataViewModel.mode == DataMode.EDIT) {
            val product = addProductDataViewModel.productById.value!!
            product.name = binding.productNameInput.text.toString()
            product.categoryId =
                addProductDataViewModel.pickedCategory?.id ?: throw NoCategoryIdException()
            product.quantity = doubleQuantityTextToInt(binding.productQuantityInput.text)
            product.unitPrice = doublePriceTextToInt(binding.productUnitPriceInput.text)
            product.subtotalPrice = doublePriceTextToInt(binding.productSubtotalPriceInput.text)
            product.discount = doublePriceTextToInt(binding.productDiscountInput.text)
            product.finalPrice = doublePriceTextToInt(binding.productFinalPriceInput.text)
            product.ptuType = binding.ptuTypeInput.text.toString()
            product.validPrice = isValidPrices
            product.tagList = binding.chipGroup.getTagIds()

            when (addProductDataViewModel.inputType) {
                AddingInputType.ID.name -> {
                    runBlocking {
                        product.originalTagList =
                            addProductDataViewModel.tagList.value?.selectedTags ?: listOf()
                        addProductDataViewModel.updateSingleProduct(product)
                        listingViewModel.loadDataByReceiptFilter()
                        listingViewModel.loadDataByProductFilter()
                        listingViewModel.loadDataByTagFilter()
                    }
                }

                AddingInputType.INDEX.name -> {
                    addProductDataViewModel.aggregatedProductList.value!![addProductDataViewModel.productIndex] =
                        product
                    listingViewModel.loadDataByCategoryFilter()
                }

                else -> {
                    throw Exception("PRODUCT INDEX IS NOT SET")
                }
            }
        }
    }

    private fun isProductIndexSet() {
        if (addProductDataViewModel.productIndex < 0) {
            throw Exception("NO PRODUCT INDEX SET: " + addProductDataViewModel.productIndex)
        }
    }

    private fun isArgSetOrThrow(arg: String?, errorMessage: String) {
        if (arg == "") {
            throw Exception(errorMessage)
        }
    }


    private fun getAllPriceInputs(): ProductInputs {
        return ProductInputs(
            quantity = binding.productQuantityInput.text,
            unitPrice = binding.productUnitPriceInput.text,
            subtotalPrice = binding.productSubtotalPriceInput.text,
            discount = binding.productDiscountInput.text,
            finalPrice = binding.productFinalPriceInput.text
        )
    }

    private fun errorTextBySuggestions(suggestion: String?): String? {
        if (suggestion == null) {
            return null
        }
        return " "
    }

    private fun validateFinalPrice() {
        val inputs = getAllPriceInputs()
        val suggestions = addProductDataViewModel.validateFinalPrices(inputs)
        binding.productSubtotalPriceHelperText.text = suggestions.subtotalPriceFirst
        binding.productSubtotalPriceSecondHelperText.text = suggestions.subtotalPriceSecond
        binding.productDiscountHelperText.text = suggestions.discount
        binding.productFinalPriceHelperText.text = suggestions.finalPrice

        binding.productSubtotalPriceLayout.error =
            errorTextBySuggestions(suggestions.subtotalPriceFirst)
        binding.productDiscountLayout.error = errorTextBySuggestions(suggestions.discount)
        binding.productFinalPriceLayout.error = errorTextBySuggestions(suggestions.finalPrice)
    }

    private fun validatePrices() {
        val inputs = getAllPriceInputs()
        val suggestions = addProductDataViewModel.validatePrices(inputs)
        binding.productQuantityHelperText.text = suggestions.quantity
        binding.productUnitPriceHelperText.text = suggestions.unitPrice
        binding.productSubtotalPriceHelperText.text = suggestions.subtotalPriceFirst
        binding.productSubtotalPriceSecondHelperText.text = suggestions.subtotalPriceSecond

        binding.productQuantityLayout.error = errorTextBySuggestions(suggestions.quantity)
        binding.productUnitPriceLayout.error = errorTextBySuggestions(suggestions.unitPrice)
        binding.productSubtotalPriceLayout.error =
            errorTextBySuggestions(suggestions.subtotalPriceFirst)

    }

    private fun validateObligatoryFields(): Boolean {
        val errors = addProductDataViewModel.validateObligatoryFields(getInputs())
        binding.productNameLayout.error = errors.name

        binding.productCategoryHelperText.text = errors.categoryName
        binding.productCategoryLayout.error = errorTextBySuggestions(errors.categoryName)


        binding.ptuTypeLayout.error = errors.ptuType

        binding.productQuantityHelperText.text = errors.quantity
        binding.productUnitPriceHelperText.text = errors.unitPrice
        binding.productSubtotalPriceHelperText.text = errors.subtotalPriceFirst
        binding.productSubtotalPriceSecondHelperText.text = errors.subtotalPriceSecond
        binding.productDiscountHelperText.text = errors.discount
        binding.productFinalPriceHelperText.text = errors.finalPrice

        binding.productQuantityLayout.error = errorTextBySuggestions(errors.quantity)
        binding.productUnitPriceLayout.error = errorTextBySuggestions(errors.unitPrice)
        binding.productSubtotalPriceLayout.error = errorTextBySuggestions(errors.subtotalPriceFirst)
        binding.productDiscountLayout.error = errorTextBySuggestions(errors.discount)
        binding.productFinalPriceLayout.error = errorTextBySuggestions(errors.finalPrice)

        binding.productQuantityLayout.errorIconDrawable = null
        binding.productUnitPriceLayout.errorIconDrawable = null
        binding.productSubtotalPriceLayout.errorIconDrawable = null
        binding.productDiscountLayout.errorIconDrawable = null
        binding.productFinalPriceLayout.errorIconDrawable = null
        return errors.isCorrect()
    }

    private fun convertSuggestionToValue(suggestion: String): String {
        return suggestion.substring(
            addProductDataViewModel.getSuggestionPrefix().length + 1, suggestion.lastIndex + 1
        )
    }

    private fun convertSecondSuggestionToValue(suggestion: String): String {
        return suggestion.substring(
            addProductDataViewModel.getSecondSuggestionPrefix().length + 1, suggestion.lastIndex + 1
        )
    }

    private fun createChips(tagList: TagList) {
        tagList.selectedTags.forEach { tag ->
            binding.chipGroup.addView(createChip(tag, true))
        }
        tagList.notSelectedTags.forEach { tag ->
            binding.chipGroup.addView(createChip(tag, false))
        }
        binding.chipGroup.addView(createAddNewChip())
    }

    private fun createAddNewChip(): Chip {
        val addTagChip = createChip(Tag(getString(R.string.add_tag)), false)
        addTagChip.isCheckable = false
        addTagChip.setOnClickListener {
            val action =
                AddProductFragmentDirections.actionAddProductFragmentToAddTagFragment(
                    tagId = "",
                    inputType = AddingInputType.EMPTY.name,
                    sourceFragment = FragmentName.ADD_PRODUCT_FRAGMENT
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        return addTagChip
    }


    private fun createChip(tag: Tag, isChecked: Boolean): Chip {
        val chip =
            layoutInflater.inflate(R.layout.single_chip_layout, binding.chipGroup, false) as Chip

        chip.apply {
            text = tag.name
            this.tag = tag
            this.isCheckable = true
            this.isChecked = isChecked
        }

        return chip
    }


    override fun initObserver() {
        addProductDataViewModel.tagList.observe(viewLifecycleOwner) {
            it?.let { tagList ->
                binding.chipGroup.removeAllViews()
                createChips(tagList)
            }
        }
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
                dropdownAdapter.updateList(categoryList)

                if (addProductDataViewModel.categoryId.isNotEmpty()) {
                    setCategory()
                }
            }
            val storeCategoryId = addProductDataViewModel.storeById.value?.defaultCategoryId
            if (storeCategoryId != null) {
                categoryList.find { category -> category.id == storeCategoryId }?.name.let { name ->
                    name?.let { addProductDataViewModel.storeDefaultCategoryName = it }
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

                validateObligatoryFields()
            }
        }
        addProductDataViewModel.storeById.observe(viewLifecycleOwner) { store ->
            store?.let {
                binding.toolbar.title = store.name
                if (addProductDataViewModel.receiptById.value == null && addProductDataViewModel.categoryId == "") {
                    addProductDataViewModel.categoryId = store.defaultCategoryId
                    setCategory()
                }
                addProductDataViewModel.categoryList.value?.find { category -> category.id == store.defaultCategoryId }?.name.let { name ->
                    name?.let { addProductDataViewModel.storeDefaultCategoryName = name }
                }
            }
        }
    }

    override fun clearInputs() {
        addProductDataViewModel.productById.value = null
        addProductDataViewModel.pickedCategory = null
        addProductDataViewModel.categoryId = ""
    }

    private fun setCategory() {
        addProductDataViewModel.pickedCategory =
            addProductDataViewModel.categoryList.value?.find { category -> category.id == addProductDataViewModel.categoryId }

        addProductDataViewModel.pickedCategory?.let {
            binding.productCategoryInput.setText(it.name)
            validateCategory()
        }
    }

    private fun validateCategory() {
        val categoryName = binding.productCategoryInput.text

        val message = addProductDataViewModel.validateCategory(categoryName)

        binding.productCategoryInput.isEnabled = message != null
        binding.productCategoryHelperText.text = message
        binding.productCategoryLayout.error = errorTextBySuggestions(message)
        binding.productCategoryLayout.errorIconDrawable = null

    }
}
