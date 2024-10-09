package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.dto.Utils.Companion.doubleToString
import com.example.navsample.dto.Utils.Companion.quantityToString
import com.example.navsample.dto.Utils.Companion.roundDouble
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val args: AddProductFragmentArgs by navArgs()

    private var ptuTypeList = arrayOf("A", "B", "C", "D", "E", "F", "G")

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var productOriginalInput = ""
    private var chosenCategory = Category("", "")
    private var productId: Int? = null

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

    private fun initObserver() {
        receiptImageViewModel.bitmapCropped.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }
        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                CategoryDropdownAdapter(
                    requireContext(), R.layout.array_adapter_row, it
                ).also { adapter ->
                    binding.productCategoryInput.setAdapter(adapter)
                }
            }
            if (chosenCategory.name == "") {
                chosenCategory = it[0]
            }
        }
        receiptDataViewModel.store.observe(viewLifecycleOwner) { store ->
            if (chosenCategory.name == "") {
                chosenCategory = try {
                    receiptDataViewModel.categoryList.value?.first { it.id == store.defaultCategoryId }
                        ?: Category("", "")
                } catch (e: Exception) {
                    Category("", "")
                }

            }
        }
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
                binding.productDiscountHelperText.text = ""
                binding.productFinalPriceHelperText.text = ""
            } else {
                binding.productDiscountHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice - finalPrice))
                binding.productFinalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice - discountPrice))
            }
        } else if (checks == 1) {
            if (discountPrice == null) {
                binding.productDiscountHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! - finalPrice!!))
            }
            if (finalPrice == null) {
                binding.productFinalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! - discountPrice!!))
            }
        } else {
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
                binding.productSubtotalPriceHelperText.text = ""
                binding.productUnitPriceHelperText.text = ""
                binding.productQuantityHelperText.text = ""
            } else {
                binding.productSubtotalPriceHelperText.text =
                    getSuggestionMessage(doubleToString(unitPrice * quantity))
                binding.productUnitPriceHelperText.text =
                    getSuggestionMessage(doubleToString(subtotalPrice!! / quantity))
                binding.productQuantityHelperText.text =
                    getSuggestionMessage(quantityToString(subtotalPrice / unitPrice))
            }
        } else if (checks == 2) {
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

        initObserver()

        binding.toolbar.title = receiptDataViewModel.store.value?.name
        receiptDataViewModel.refreshCategoryList()
        receiptDataViewModel.categoryList.value?.let {
            if (chosenCategory.name == "") {
                val categoryId = receiptDataViewModel.store.value?.defaultCategoryId ?: 0

                chosenCategory = try {
                    receiptDataViewModel.categoryList.value?.first { it.id == categoryId }
                        ?: Category("", "")
                } catch (e: Exception) {
                    Category("", "")
                }

            }
        }

        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, ptuTypeList
        ).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }

        if (args.productIndex != -1) {
            val productOriginal = receiptDataViewModel.product.value?.get(args.productIndex)
            productId = productOriginal?.id
            productOriginal?.let { product ->
                val category = try {
                    receiptDataViewModel.categoryList.value?.first { it.id == product.categoryId }
                } catch (e: Exception) {
                    null
                }
                if (category != null) {
                    chosenCategory = category
                }

                binding.productNameInput.setText(product.name)
                binding.productUnitPriceInput.setText(doubleToString(product.unitPrice))
                binding.productQuantityInput.setText(quantityToString(product.quantity))
                binding.productSubtotalPriceInput.setText(doubleToString(product.subtotalPrice))
                binding.productDiscountInput.setText(doubleToString(product.discount))
                binding.productFinalPriceInput.setText(doubleToString(product.finalPrice))
                binding.ptuTypeInput.setText(product.ptuType)
                binding.productCategoryInput.setText(chosenCategory.name)
                binding.productOriginalInput.setText(product.raw)
                productOriginalInput = product.raw

                validatePrices()
                validateFinalPrice()
            }
        } else {
            binding.productCategoryInput.setText(chosenCategory.name)

        }
        if (productOriginalInput == "") {
            binding.productOriginalLayout.visibility = View.INVISIBLE
        }
        binding.productCategoryInput.setOnItemClickListener { adapter, _, i, _ ->
            chosenCategory = adapter.getItemAtPosition(i) as Category
            binding.productCategoryInput.setText(chosenCategory.name)
        }

        binding.productCategoryLayout.setStartIconOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_addProductFragment_to_addCategoryFragment)

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
                receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException(),
                receiptDataViewModel.store.value?.defaultCategoryId ?: throw NoStoreIdException()
            )
            val product = receiptParser.parseStringToProduct(actual.toString())
            val category = try {
                receiptDataViewModel.categoryList.value?.first { it.id == product.categoryId }
            } catch (e: Exception) {
                null
            }
            if (category != null) {
                chosenCategory = category
            }
            binding.productNameInput.setText(product.name)
            binding.productSubtotalPriceInput.setText(product.subtotalPrice.toString())
            binding.productUnitPriceInput.setText(product.unitPrice.toString())
            binding.productQuantityInput.setText(product.quantity.toString())
            binding.ptuTypeInput.setText(product.ptuType)
            binding.productCategoryInput.setText(chosenCategory.name)

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

                    val product = Product(
                        receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException(),
                        binding.productNameInput.text.toString(),
                        chosenCategory.id ?: throw NoCategoryIdException(),
                        binding.productQuantityInput.text.toString().toDouble(),
                        binding.productUnitPriceInput.text.toString().toDouble(),
                        binding.productSubtotalPriceInput.text.toString().toDouble(),
                        binding.productDiscountInput.text.toString().toDouble(),
                        binding.productFinalPriceInput.text.toString().toDouble(),
                        binding.ptuTypeInput.text.toString(),
                        binding.productOriginalInput.text.toString()
                    )
                    product.id = productId

                    if (args.productIndex != -1) {
                        receiptDataViewModel.product.value!![args.productIndex] = product
                    } else {
                        receiptDataViewModel.product.value!!.add(product)
                    }
                    if (args.saveProduct) {
                        receiptDataViewModel.insertProducts(listOf(product))
                    }
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

    private fun convertSuggestionToValue(suggestion: String): String {
        return suggestion.substring(
            SUGGESTION_PREFIX.length, suggestion.lastIndex + 1
        )
    }
}
