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

    val args: AddProductFragmentArgs by navArgs()

    var picker: TimePickerDialog? = null
    var calendar = Calendar.getInstance()
    private lateinit var categoryList: List<String>
    private var addNewCategory = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun decodeReceiptId(receiptId: String?) {
        if (receiptId != null) {
            val split = receiptId.split("_")
            if (split.size == 3) {
                binding.storeNameInput.setText(split[0])
                binding.receiptDateInput.text = split[1]
                binding.receiptTimeInput.text = split[2]
            } else {
                binding.storeNameInput.setText("")
                binding.receiptDateInput.text = "01-01-1970"
                binding.receiptTimeInput.text = "00:00"
            }
        }
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
        binding.productPriceInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val actual = binding.productPriceInput.text.toString()
                val split = actual.split(".")
                if (split.size >= 2 && (split[1].length > 2 || split[1].length == 0)) {
                    var fixed = split[0]
                    if (split[1].length > 2) {
                        fixed = split[0] + "." + split[1].substring(0, 2)
                    }
                    binding.productPriceInput.setText(fixed)
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
            binding.productPriceInput.setText(args.product!!.finalPrice.toString())
            binding.productCategoryInput.setText(args.product!!.category)



            decodeReceiptId(args.product!!.receiptId)
        } else {
            binding.receiptDateInput.text =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            binding.receiptTimeInput.text =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        binding.receiptDateInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.receiptTimeInput.setOnClickListener {
            val hour = binding.receiptTimeInput.text.subSequence(0, 2).toString().toInt()
            val minutes = binding.receiptTimeInput.text.subSequence(3, 5).toString().toInt()
            // time picker dialog
            picker = TimePickerDialog(
                requireContext(), { _, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                }, hour, minutes, true
            )
            picker!!.show()
        }

        binding.cancelAddProductButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_addProductFragment_to_shopListFragment)
        }

        binding.confirmAddProductButton.setOnClickListener {
            val receiptId: String =
                binding.storeNameInput.text.toString() + "_" + binding.receiptDateInput.text.toString() + "_" + binding.receiptTimeInput.text.toString()
            val product = Product(
                null,
                receiptId,
                binding.productNameInput.text.toString(),
                binding.productPriceInput.text.toString(),
                binding.productCategoryInput.text.toString(),
                null,null,null
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

    private fun setHourAndMinutes(sHour: Int, sMinute: Int) {
        val hourValue = if (sHour.toString().length == 1) {
            "0$sHour"
        } else {
            sHour.toString()
        }
        val minuteValue = if (sMinute.toString().length == 1) {
            "0$sMinute"
        } else {
            sMinute.toString()
        }

        binding.receiptTimeInput.text = "$hourValue:$minuteValue"
    }

    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.receiptDateInput.text = sdf.format(calendar.time)
    }
}