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
import com.example.navsample.DTO.ChartColors
import com.example.navsample.R
import com.example.navsample.ReceiptParser
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddProductBinding
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
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
                    requireContext(),
                    R.layout.array_adapter_row,
                    it
                ).also { adapter ->
                    binding.productCategoryInput.setAdapter(adapter)
                }
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
                binding.productFinalPriceLayout.helperText = null
                binding.productItemPriceLayout.helperText = null
                binding.productAmountLayout.helperText = null
            } else {
                binding.productFinalPriceLayout.helperText =
                    "Maybe ${round(itemPrice * amount * 100) / 100}"
                binding.productItemPriceLayout.helperText =
                    "Maybe ${round(finalPrice / amount * 100) / 100}"
                binding.productAmountLayout.helperText =
                    "Maybe ${round(finalPrice / itemPrice * 100) / 100}"
            }
        } else if (checks == 2) {
            if (finalPrice < 0) {
                binding.productFinalPriceLayout.helperText =
                    "Maybe ${round(itemPrice * amount * 100) / 100}"
            }
            if (itemPrice < 0) {
                binding.productItemPriceLayout.helperText =
                    "Maybe ${round(finalPrice / amount * 100) / 100}"
            }
            if (amount < 0) {
                binding.productAmountLayout.helperText =
                    "Maybe ${round(finalPrice / itemPrice * 100) / 100}"
            }
        } else {
            binding.productFinalPriceLayout.helperText = null
            binding.productItemPriceLayout.helperText = null
            binding.productAmountLayout.helperText = null

        }
    }

    private fun validateObligatoryFields(): Boolean {
        var succeedValidation = true
        if (binding.productNameInput.text.isNullOrEmpty()) {
            binding.productNameLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.productFinalPriceInput.text.isNullOrEmpty()) {
            binding.productFinalPriceLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.productItemPriceInput.text.isNullOrEmpty()) {
            binding.productItemPriceLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.productAmountInput.text.isNullOrEmpty()) {
            binding.productAmountLayout.error = "Empty"
            succeedValidation = false
        }
        return succeedValidation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiptDataViewModel.refreshCategoryList()
        initObserver()
        validatePrices()

        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, ptuTypeList
        ).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }

        if (args.productIndex != -1) {
            productOriginal = receiptDataViewModel.product.value?.get(args.productIndex)


            productOriginal?.let { product ->
                val category = try {
                    receiptDataViewModel.categoryList.value?.filter { it.id == product.categoryId }
                        ?.first()
                } catch (e: Exception) {
                    receiptDataViewModel.categoryList.value!![0]
                }

                binding.productNameInput.setText(product.name)
                binding.productFinalPriceInput.setText(product.finalPrice.toString())
                binding.productItemPriceInput.setText(product.itemPrice.toString())
                binding.productAmountInput.setText(product.amount.toString())
                binding.ptuTypeInput.setText(product.ptuType)
                binding.productCategoryInput.setText(category?.name)
                binding.productOriginalInput.setText(product.raw)
                productOriginalInput = product.raw

            }
        }
        if (productOriginalInput == "") {
            binding.productOriginalLayout.visibility = View.INVISIBLE
        }
        binding.productCategoryInput.setOnItemClickListener { adapter, _, i, _ ->
            val category = adapter.getItemAtPosition(i) as Category
            binding.productCategoryInput.setText(category.name)
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
                binding.productFinalPriceLayout.error = null
                validatePrices()
            }
        }
        binding.productItemPriceInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productItemPriceLayout.error = null
                validatePrices()
            }
        }
        binding.productAmountInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.productAmountLayout.error = null
                validatePrices()
            }
        }
        binding.productOriginalInput.doOnTextChanged { actual, _, _, _ ->
            val receiptParser = ReceiptParser()
            val product = receiptParser.parseStringToProduct(actual.toString())
            val category =
                receiptDataViewModel.categoryList.value?.filter { it.id == product.categoryId }
                    ?.first()

            binding.productNameInput.setText(product.name)
            binding.productFinalPriceInput.setText(product.finalPrice.toString())
            binding.productItemPriceInput.setText(product.itemPrice.toString())
            binding.productAmountInput.setText(product.amount.toString())
            binding.ptuTypeInput.setText(product.ptuType)
            binding.productCategoryInput.setText(category?.name)

        }

        binding.cancelAddProductButton.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.confirmAddProductButton.setOnClickListener {
            if (!validateObligatoryFields()) {
                return@setOnClickListener
            }

            val category =
                Category(
                    binding.productCategoryInput.text.toString(),
                    ChartColors.DEFAULT_CATEGORY_COLOR_STRING
                )

            val product = Product(
                productOriginal?.receiptId ?: -1,
                binding.productNameInput.text.toString(),
                category.id ?: 0,
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

            Navigation.findNavController(requireView()).popBackStack()
        }
    }
}
