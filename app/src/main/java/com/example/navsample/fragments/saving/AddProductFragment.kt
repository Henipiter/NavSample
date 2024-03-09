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
    private var productOriginal: Product? = null
    private var chosenCategory = Category("", "")

    companion object {
        private val SUGGESTION_PREFIX = "Maybe "
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
        val finalPrice = tryConvertToDouble(binding.productFinalPriceInput.text.toString())
        val itemPrice = tryConvertToDouble(binding.productItemPriceInput.text.toString())
        val amount = tryConvertToDouble(binding.productAmountInput.text.toString())

        val checks = arrayOf(finalPrice, itemPrice, amount).count { it >= 0 }
        if (checks == 3) {
            if (itemPrice * amount == finalPrice) {
                binding.productFinalPriceHelperText.text = ""
                binding.productItemPriceHelperText.text = ""
                binding.productAmountHelperText.text = ""
            } else {
                binding.productFinalPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(itemPrice * amount * 100) / 100}"
                binding.productItemPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(finalPrice / amount * 100) / 100}"
                binding.productAmountHelperText.text =
                    "${SUGGESTION_PREFIX}${round(finalPrice / itemPrice * 100) / 100}"
            }
        } else if (checks == 2) {
            if (finalPrice < 0) {
                binding.productFinalPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(itemPrice * amount * 100) / 100}"
            }
            if (itemPrice < 0) {
                binding.productItemPriceHelperText.text =
                    "${SUGGESTION_PREFIX}${round(finalPrice / amount * 100) / 100}"
            }
            if (amount < 0) {
                binding.productAmountHelperText.text =
                    "${SUGGESTION_PREFIX}${round(finalPrice / itemPrice * 100) / 100}"
            }
        } else {
            binding.productFinalPriceHelperText.text = ""
            binding.productItemPriceHelperText.text = ""
            binding.productAmountHelperText.text = ""

        }
    }


    private fun validateObligatoryFields(): Boolean {
        var succeedValidation = true
        if (binding.productNameInput.text.isNullOrEmpty()) {
            binding.productNameLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.productFinalPriceInput.text.isNullOrEmpty()) {
            binding.productFinalPriceHelperText.text = "Empty"
            succeedValidation = false
        }
        if (binding.productItemPriceInput.text.isNullOrEmpty()) {
            binding.productItemPriceHelperText.text = "Empty"
            succeedValidation = false
        }
        if (binding.productAmountInput.text.isNullOrEmpty()) {
            binding.productAmountHelperText.text = "Empty"
            succeedValidation = false
        }
        return succeedValidation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiptDataViewModel.refreshCategoryList()
        initObserver()

        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, ptuTypeList
        ).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }

        if (args.productIndex != -1) {
            productOriginal = receiptDataViewModel.product.value?.get(args.productIndex)
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
                binding.productFinalPriceInput.setText(product.finalPrice.toString())
                binding.productItemPriceInput.setText(product.itemPrice.toString())
                binding.productAmountInput.setText(product.amount.toString())
                binding.ptuTypeInput.setText(product.ptuType)
                binding.productCategoryInput.setText(chosenCategory.name)
                binding.productOriginalInput.setText(product.raw)
                productOriginalInput = product.raw

                validatePrices()
            }
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
        binding.productFinalPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productFinalPriceHelperText.text = ""
                validatePrices()
            }
        }
        binding.productItemPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productItemPriceHelperText.text = ""
                validatePrices()
            }
        }
        binding.productAmountInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productAmountHelperText.text = ""
                validatePrices()
            }
        }
        binding.productOriginalInput.doOnTextChanged { actual, _, _, _ ->
            val receiptParser = ReceiptParser(
                receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException()
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
            binding.productFinalPriceInput.setText(product.finalPrice.toString())
            binding.productItemPriceInput.setText(product.itemPrice.toString())
            binding.productAmountInput.setText(product.amount.toString())
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
                productOriginal?.receiptId ?: throw NoReceiptIdException(),
                binding.productNameInput.text.toString(),
                chosenCategory.id ?: throw NoCategoryIdException(),
                binding.productFinalPriceInput.text.toString().toFloat(),
                binding.productAmountInput.text.toString().toFloat(),
                binding.productItemPriceInput.text.toString().toFloat(),
                binding.ptuTypeInput.text.toString(),
                binding.productOriginalInput.text.toString()
            )
            product.id = productOriginal?.id

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

        binding.productFinalPriceHelperText.setOnClickListener {
            if (binding.productFinalPriceHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productFinalPriceInput.setText(
                    convertSuggestionToValue(binding.productFinalPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productItemPriceHelperText.setOnClickListener {
            if (binding.productItemPriceHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productItemPriceInput.setText(
                    convertSuggestionToValue(binding.productItemPriceHelperText.text.toString())
                )
                validatePrices()
            }
        }
        binding.productAmountHelperText.setOnClickListener {
            if (binding.productAmountHelperText.text.toString().contains(SUGGESTION_PREFIX)) {
                binding.productAmountInput.setText(
                    convertSuggestionToValue(binding.productAmountHelperText.text.toString())
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
