package com.example.navsample.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.DTO.Category
import com.example.navsample.DTO.Product
import com.example.navsample.DatabaseHelper
import com.example.navsample.R
import com.example.navsample.databinding.FragmentAddProductBinding
import java.util.Date
import java.util.Locale

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val args: AddProductFragmentArgs by navArgs()

    private lateinit var categoryList: List<String>
    private var ptuTypeList = arrayOf("A", "B","C","D","E","F","G")
    private var addNewCategory = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val databaseHelper = DatabaseHelper(requireContext())
        categoryList = databaseHelper.readAllCategoryData().map { it.category ?: "null" }

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categoryList
        ).also { adapter ->
            binding.productCategoryInput.setAdapter(adapter)
        }
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            ptuTypeList
        ).also { adapter ->
            binding.ptuTypeInput.setAdapter(adapter)
        }
        binding.productCategoryInput.setOnLongClickListener {
            Toast.makeText(requireContext(), "CLICK", Toast.LENGTH_SHORT).show()
            true
        }

        binding.productCategoryInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val actual = binding.productCategoryInput.text.toString()
                val fixed = actual.replace(" ", "_").uppercase()
                if (actual != fixed) {
                    binding.productCategoryInput.setText(actual)
                    Toast.makeText(
                        requireContext(),
                        "Only uppercase without spaces",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (!categoryList.contains(fixed)) {
                    addNewCategory = true
                    binding.productCategoryInputInfo.visibility = View.VISIBLE
                } else {
                    addNewCategory = false
                    binding.productCategoryInputInfo.visibility = View.INVISIBLE
                }
            }
        }
        binding.productFinalPriceInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val actual = binding.productFinalPriceInput.text.toString()
                val split = actual.split(".")
                if (split.size >= 2 && (split[1].length > 2 || split[1].isEmpty())) {
                    var fixed = split[0]
                    if (split[1].length > 2) {
                        fixed = split[0] + "." + split[1].substring(0, 2)
                    }
                    binding.productFinalPriceInput.setText(fixed)
                    Toast.makeText(
                        requireContext(),
                        "Max 2 numbers after delimiter is valid",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        if (args.product != null) {
            binding.productNameInput.setText(args.product!!.name)
            binding.productFinalPriceInput.setText(args.product!!.finalPrice.toString())
            binding.productItemPriceInput.setText(args.product!!.itemPrice.toString())
            binding.productAmountInput.setText(args.product!!.amount.toString())
            binding.ptuTypeInput.setText(args.product!!.ptuType.toString())
            binding.productCategoryInput.setText(args.product!!.category)
        }
        else{
            binding.ptuTypeInput.setText("A")
        }

        binding.cancelAddProductButton.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.confirmAddProductButton.setOnClickListener {
            val receiptId = "rId"
            val product = Product(
                null,
                receiptId,
                binding.productNameInput.text.toString(),
                binding.productFinalPriceInput.text.toString(),
                binding.productCategoryInput.text.toString(),
                null, null, null
            )
            val category = Category(null, binding.productCategoryInput.text.toString())
            if (addNewCategory) {
                databaseHelper.addCategory(category)
            }
            databaseHelper.addProduct(product)

            Navigation.findNavController(it)
                .navigate(R.id.action_addProductFragment_to_shopListFragment)

        }
    }
}
