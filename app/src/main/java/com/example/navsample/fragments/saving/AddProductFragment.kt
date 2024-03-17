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
import com.example.navsample.ReceiptParser
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import kotlin.math.round

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
    private var receiptId: Int? = null

    companion object {
        private const val SUGGESTION_PREFIX = "Maybe "
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initObserver() {
        receiptImageViewModel.bitmapCropped.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmapCropped.value != null) {
                    binding.receiptImageBig.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
                }
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
    }

    private fun tryConvertToDouble(correctString: String): Double {
        return try {
            correctString.toDouble()
        } catch (exc: Exception) {
            -1.0
        }
    }

    private fun validatePrices() {
        val subtotalPrice = tryConvertToDouble(binding.productSubtotalPriceInput.text.toString())
        val unitPrice = tryConvertToDouble(binding.productUnitPriceInput.text.toString())
        val quantity = tryConvertToDouble(binding.productQuantityInput.text.toString())

        val checks = arrayOf(subtotalPrice, unitPrice, quantity).count { it >= 0 }
        if (checks == 3) {
            if (unitPrice * quantity == subtotalPrice) {
                binding.productSubtotalPriceHelperText.text = ""
                binding.productUnitPriceHelperText.text = ""
                binding.productQuantityHelperText.text = ""
            } else {
                binding.productSubtotalPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(unitPrice * quantity * 100) / 100}"
                binding.productUnitPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(subtotalPrice / quantity * 100) / 100}"
                binding.productQuantityHelperText.text =
                    "${SUGGESTION_PREFIX}${round(subtotalPrice / unitPrice * 100) / 100}"
            }
        } else if (checks == 2) {
            if (subtotalPrice < 0) {
                binding.productSubtotalPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(unitPrice * quantity * 100) / 100}"
            }
            if (unitPrice < 0) {
                binding.productUnitPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(subtotalPrice / quantity * 100) / 100}"
            }
            if (quantity < 0) {
                binding.productQuantityHelperText.text =
                    "${SUGGESTION_PREFIX}${round(subtotalPrice / unitPrice * 100) / 100}"
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
            binding.productSubtotalPriceHelperText.text = "Empty"
            succeedValidation = false
        }
        if (binding.productUnitPriceInput.text.isNullOrEmpty()) {
            binding.productUnitPriceHelperText.text = "Empty"
            succeedValidation = false
        }
        if (binding.productQuantityInput.text.isNullOrEmpty()) {
            binding.productQuantityHelperText.text = "Empty"
            succeedValidation = false
        }
        return succeedValidation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        receiptDataViewModel.refreshCategoryList()
        receiptDataViewModel.categoryList.value?.let {
            if (chosenCategory.name == "") {
                val categoryId = receiptDataViewModel.store.value?.defaultCategoryId
                    ?: throw NoStoreIdException()

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

        receiptId = receiptDataViewModel.receipt.value?.id
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
                binding.productUnitPriceInput.setText(product.unitPrice.toString())
                binding.productQuantityInput.setText(product.quantity.toString())
                binding.productSubtotalPriceInput.setText(product.subtotalPrice.toString())
                binding.productDiscountInput.setText(product.discount.toString())
                binding.productFinalPriceInput.setText(product.finalPrice.toString())
                binding.ptuTypeInput.setText(product.ptuType)
                binding.productCategoryInput.setText(chosenCategory.name)
                binding.productOriginalInput.setText(product.raw)
                productOriginalInput = product.raw

                validatePrices()
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

        binding.cancelAddProductButton.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.confirmAddProductButton.setOnClickListener {
            if (!validateObligatoryFields()) {
                return@setOnClickListener
            }

            val product = Product(
                receiptId ?: throw NoReceiptIdException(),
                binding.productNameInput.text.toString(),
                chosenCategory.id ?: throw NoCategoryIdException(),
                binding.productSubtotalPriceInput.text.toString().toFloat(),
                binding.productQuantityInput.text.toString().toFloat(),
                binding.productUnitPriceInput.text.toString().toFloat(),
                binding.productDiscountInput.text.toString().toFloat(),
                binding.productFinalPriceInput.text.toString().toFloat(),
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
    }

    private fun convertSuggestionToValue(suggestion: String): String {
        return suggestion.substring(
            SUGGESTION_PREFIX.length, suggestion.lastIndex + 1
        )
    }
}
