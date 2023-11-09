package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.databinding.FragmentAddSingleProductBinding
import com.example.navsample.entities.Category
import com.example.navsample.viewmodels.ReceiptDataViewModel
import kotlin.math.round

class AddSingleProductFragment : Fragment() {

    private var _binding: FragmentAddSingleProductBinding? = null
    private val binding get() = _binding!!

    private val args: AddSingleProductFragmentArgs by navArgs()

    private var ptuTypeList = arrayOf("A", "B", "C", "D", "E", "F", "G")

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSingleProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initObserver() {
        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it
                ).also { adapter ->
                    binding.productCategoryInput.setAdapter(adapter)
                }
            }
        }
    }

    private fun validatePrices() {
        val isFinalPriceCorrect = !binding.productFinalPriceInput.text.isNullOrEmpty()
        val isItemPriceCorrect = !binding.productItemPriceInput.text.isNullOrEmpty()
        val isAmountCorrect = !binding.productAmountInput.text.isNullOrEmpty()
        val checks = arrayOf(isItemPriceCorrect, isFinalPriceCorrect, isAmountCorrect).count { it }
        if (checks == 3) {
            val finalPrice = binding.productFinalPriceInput.text.toString().toFloat()
            val itemPrice = binding.productItemPriceInput.text.toString().toFloat()
            val amount = binding.productAmountInput.text.toString().toFloat()
            if (itemPrice * amount == finalPrice) {
                binding.productFinalPriceLayout.helperText = null
                binding.productItemPriceLayout.helperText = null
                binding.productAmountLayout.helperText = null
            } else {
                binding.productFinalPriceLayout.helperText =
                    "Maybe ${round((itemPrice * amount * 100).toDouble()) / 100}"
                binding.productItemPriceLayout.helperText =
                    "Maybe ${round((finalPrice / amount * 100).toDouble()) / 100}"
                binding.productAmountLayout.helperText =
                    "Maybe ${round((finalPrice / itemPrice * 100).toDouble()) / 100}"
            }
        } else if (checks == 2) {
            if (!isFinalPriceCorrect) {
                val itemPrice = binding.productItemPriceInput.text.toString().toFloat()
                val amount = binding.productAmountInput.text.toString().toFloat()
                binding.productFinalPriceLayout.helperText =
                    "Maybe ${round((itemPrice * amount * 100).toDouble()) / 100}"
            }
            if (!isItemPriceCorrect) {
                val finalPrice = binding.productFinalPriceInput.text.toString().toFloat()
                val amount = binding.productAmountInput.text.toString().toFloat()
                binding.productItemPriceLayout.helperText =
                    "Maybe ${round((finalPrice / amount * 100).toDouble()) / 100}"
            }
            if (!isAmountCorrect) {
                val finalPrice = binding.productFinalPriceInput.text.toString().toFloat()
                val itemPrice = binding.productItemPriceInput.text.toString().toFloat()
                binding.productAmountLayout.helperText =
                    "Maybe ${round((finalPrice / itemPrice * 100).toDouble()) / 100}"
            }
        } else {
            binding.productFinalPriceLayout.helperText = null
            binding.productItemPriceLayout.helperText = null
            binding.productAmountLayout.helperText = null

        }
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

        binding.productCategoryInput.setOnLongClickListener {
            Toast.makeText(requireContext(), "CLICK", Toast.LENGTH_SHORT).show()
            true
        }

        binding.productCategoryInput.doOnTextChanged { actual, start, before, count ->
            if (actual.toString().contains(" ") || Regex(".*[a-z].*").matches(actual.toString())) {
                val fixed = actual.toString().replace(" ", "_").uppercase()
                binding.productCategoryInput.setText(fixed)
                binding.productCategoryInput.setSelection(start + count)
            }
            if (receiptDataViewModel.categoryList.value?.contains(binding.productCategoryInput.text.toString()) == false) {
                binding.productCategoryLayout.helperText = "New category will be added"
            } else {
                binding.productCategoryLayout.helperText = null
            }
        }

        binding.productFinalPriceInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
        }
        binding.productItemPriceInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
        }
        binding.productAmountInput.doOnTextChanged { _, _, _, _ ->
            validatePrices()
        }


        if (args.productIndex != -1) {
            val product = receiptDataViewModel.product.value?.get(args.productIndex)
            product?.let {
                binding.productNameInput.setText(product.name)
                binding.productFinalPriceInput.setText(product.finalPrice.toString())
                binding.productItemPriceInput.setText(product.itemPrice.toString())
                binding.productAmountInput.setText(product.amount.toString())
                binding.ptuTypeInput.setText(product.ptuType.toString())
                binding.productCategoryInput.setText(product.category)
            }
        }

        binding.cancelAddProductButton.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.confirmAddProductButton.setOnClickListener {
            val category = Category(binding.productCategoryInput.text.toString())


            if (receiptDataViewModel.categoryList.value?.contains(category.name) == false) {
                receiptDataViewModel.insertCategoryList(category)
            }

            val product = ProductDTO(
                null,
                null,
                binding.productNameInput.text.toString(),
                binding.productFinalPriceInput.text.toString(),
                category.name,
                binding.productAmountInput.text.toString(),
                binding.productItemPriceInput.text.toString(),
                binding.ptuTypeInput.text.toString()
            )

            if (args.productIndex != -1) {
                receiptDataViewModel.product.value!![args.productIndex] = product
            } else {
                receiptDataViewModel.product.value!!.add(product)
            }

            Navigation.findNavController(requireView()).popBackStack()
        }
    }
}
