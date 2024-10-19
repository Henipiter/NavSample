package com.example.navsample.fragments.saving

import android.os.Bundle
import android.util.Log
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
import com.example.navsample.adapters.StoreDropdownAdapter
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddReceiptFragment : Fragment() {
    private var _binding: FragmentAddReceiptBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddReceiptFragmentArgs by navArgs()
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val addReceiptDataViewModel: AddReceiptDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()


    private var calendarDate = Calendar.getInstance()
    private var calendarTime = Calendar.getInstance()
    private lateinit var dropdownAdapter: StoreDropdownAdapter
    private var mode = DataMode.NEW
    private var pickedStore: Store? = null
    private var goNext = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false


        dropdownAdapter = StoreDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeNameInput.setAdapter(adapter)
        }

        addReceiptDataViewModel.receiptById.value = null
        initObserver()
        addReceiptDataViewModel.refreshStoreList()
        consumeNavArgs()

        imageViewModel.bitmapCroppedReceipt.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }

        defineClickListeners()
    }

    private fun consumeNavArgs() {
        val inputType = AddingInputType.getByName(navArgs.inputType)
        if (inputType == AddingInputType.EMPTY) {
            mode = DataMode.NEW
            binding.storeNameInput.setText("")
            binding.receiptPLNInput.setText("")
            binding.receiptPTUInput.setText("")
            binding.receiptDateInput.setText("")
            binding.receiptTimeInput.setText("")
            binding.toolbar.title = "Add receipt"

        } else if (inputType == AddingInputType.ID) {
            if (navArgs.receiptId >= 0) {
                binding.toolbar.title = "Edit receipt"
                mode = DataMode.EDIT
                addReceiptDataViewModel.getReceiptById(navArgs.receiptId)
            } else {
                throw Exception("NO RECEIPT ID SET: " + navArgs.receiptId)
            }
        } else {
            throw Exception("BAD INPUT TYPE: " + navArgs.inputType)
        }
    }

    private fun initObserver() {
        imageViewModel.bitmapCroppedReceipt.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(imageViewModel.bitmapCroppedReceipt.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }
        addReceiptDataViewModel.storeList.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                dropdownAdapter.storeList = storeList
                dropdownAdapter.notifyDataSetChanged()
            }
        }
        addReceiptDataViewModel.receiptById.observe(viewLifecycleOwner) {
            it?.let { receipt ->
                val store = try {
                    addReceiptDataViewModel.storeList.value?.first { store -> store.id == receipt.storeId }
                } catch (e: Exception) {
                    null
                }
                binding.storeNameInput.setText(store?.name)
                binding.receiptPLNInput.setText(receipt.pln.toString())
                binding.receiptPTUInput.setText(receipt.ptu.toString())
                binding.receiptDateInput.setText(receipt.date)
                binding.receiptTimeInput.setText(receipt.time)
            }
        }

        imageAnalyzerViewModel.receiptAnalyzed.observe(viewLifecycleOwner) { analyzedReceipt ->
            if (analyzedReceipt == null) {
                return@observe
            }
            Log.i("ImageProcess", "valueNIP ${analyzedReceipt.valueNIP}")
            Log.i("ImageProcess", "companyName ${analyzedReceipt.companyName}")
            Log.i("ImageProcess", "valuePTU ${analyzedReceipt.valuePTU}")
            Log.i("ImageProcess", "valuePLN ${analyzedReceipt.valuePLN}")
            Log.i("ImageProcess", "valueDate ${analyzedReceipt.valueDate}")
            Log.i("ImageProcess", "valueTime ${analyzedReceipt.valueTime}")
            imageAnalyzerViewModel.receiptAnalyzed.value = null

            binding.receiptPLNInput.setText(analyzedReceipt.valuePLN.toString())
            binding.receiptPTUInput.setText(analyzedReceipt.valuePTU.toString())
            binding.receiptDateInput.setText(analyzedReceipt.valueDate)
            binding.receiptTimeInput.setText(analyzedReceipt.valueTime)
            binding.toolbar.title = "Add store"

            val foundStore =
                addReceiptDataViewModel.storeList.value?.find { it.nip == analyzedReceipt.valueNIP }
            if (foundStore == null) {
                binding.storeNameInput.setText("")
                val action =
                    AddReceiptFragmentDirections.actionAddReceiptFragmentToAddStoreFragment(
                        inputType = AddingInputType.FIELD.name,
                        storeName = analyzedReceipt.companyName,
                        storeNip = analyzedReceipt.valueNIP
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                pickedStore = foundStore
                binding.storeNameInput.setText(foundStore.name)
                binding.storeNameInput.isEnabled = false
            }


        }
        addReceiptDataViewModel.savedReceipt.observe(viewLifecycleOwner) {
            it?.let {
                val action =
                    AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment(
                        receiptId = it.id!!,
                        storeId = it.storeId
                    )
                Navigation.findNavController(requireView()).navigate(action)
            }
        }
    }

    private fun transformToDouble(value: String): Double {
        return try {
            value.replace(",", ".").toDouble()
        } catch (t: Throwable) {
            0.0
        }
    }


    private fun validateObligatoryFields(): Boolean {
        var succeedValidation = true
        if (binding.receiptPLNInput.text.isNullOrEmpty()) {
            binding.receiptPLNLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.receiptPTUInput.text.isNullOrEmpty()) {
            binding.receiptPTULayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.receiptDateInput.text.isNullOrEmpty()) {
            binding.receiptDateLayout.error = "Empty"
            succeedValidation = false
        }
        if (binding.receiptTimeInput.text.isNullOrEmpty()) {
            binding.receiptTimeLayout.error = "Empty"
            succeedValidation = false
        }
        return succeedValidation
    }

    private fun isReceiptInputValid(): Boolean {
        if (pickedStore == null || pickedStore?.id == null) {
            Toast.makeText(requireContext(), "Pick store!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!validateObligatoryFields()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveChangesToDatabase() {
        val pln = transformToDouble(binding.receiptPLNInput.text.toString())
        val ptu = transformToDouble(binding.receiptPTUInput.text.toString())
        val date = binding.receiptDateInput.text.toString()
        val time = binding.receiptTimeInput.text.toString()

        if (mode == DataMode.NEW) {
            pickedStore?.id?.let { storeId ->
                val receipt = Receipt(-1, -1.0, -1.0, "", "", true).apply {
                    this.storeId = storeId
                    this.pln = pln
                    this.ptu = ptu
                    this.date = date
                    this.time = time
                }
                addReceiptDataViewModel.insertReceipt(receipt)
                //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
                listingViewModel.loadDataByReceiptFilter()
                listingViewModel.loadDataByProductFilter()
                goNext = true
            }
        } else if (mode == DataMode.EDIT) {
            addReceiptDataViewModel.receiptById.value?.let {
                pickedStore?.id?.let { storeId ->
                    val receipt = Receipt(-1, -1.0, -1.0, "", "", true).apply {
                        this.id = it.id
                        this.storeId = storeId
                        this.pln = pln
                        this.ptu = ptu
                        this.date = date
                        this.time = time
                    }
                    addReceiptDataViewModel.updateReceipt(receipt)
                    listingViewModel.loadDataByReceiptFilter()
                    goNext = false
                }
            }

        }
    }


    private fun defineClickListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!isReceiptInputValid()) {
                        return@setOnMenuItemClickListener false
                    }
                    saveChangesToDatabase()
                    return@setOnMenuItemClickListener true
                }

                R.id.add_new -> {
                    addReceiptDataViewModel.receiptById.value?.let { receipt ->
                        val action =
                            AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment(
                                receiptId = receipt.id!!,
                                storeId = receipt.storeId
                            )
                        Navigation.findNavController(requireView()).navigate(action)
                        return@setOnMenuItemClickListener true
                    }
                    Toast.makeText(requireContext(), "Receipt not set", Toast.LENGTH_SHORT)

                    return@setOnMenuItemClickListener true
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            binding.storeNameLayout.helperText = null
            binding.storeNameInput.isEnabled = true
            pickedStore = null
        }

        binding.storeNameInput.setOnItemClickListener { adapter, _, position, _ ->
            val store = adapter.getItemAtPosition(position) as Store
            if ("" == store.nip && binding.storeNameInput.adapter.count - 1 == position) {
                binding.storeNameInput.setText("")

                val action =
                    AddReceiptFragmentDirections.actionAddReceiptFragmentToAddStoreFragment(
                        storeName = null,
                        storeNip = null
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                pickedStore = store
                binding.storeNameInput.setText(store.name)
                binding.storeNameInput.isEnabled = false
            }

        }

        binding.receiptDateLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.receiptDateInput.setOnClickListener {
            showDatePicker()
        }
        binding.receiptTimeLayout.setEndIconOnClickListener {
            showTimePicker()
        }
        binding.receiptTimeInput.setOnClickListener {
            showTimePicker()
        }

        binding.receiptDateInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptDateLayout.error = null
            }
        }
        binding.receiptTimeInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptTimeInput.error = null
            }
        }
        binding.receiptPLNInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptPLNLayout.error = null
            }
        }
        binding.receiptPTUInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptPTULayout.error = null
            }
        }
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendarTime.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendarTime.get(Calendar.MINUTE)).build()

        timePicker.show(childFragmentManager, "Test")
        timePicker.addOnPositiveButtonClickListener {
            calendarTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendarTime.set(Calendar.MINUTE, timePicker.minute)
            val time = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.receiptTimeInput.setText(time)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(calendarDate.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")

        datePicker.addOnPositiveButtonClickListener {
            calendarDate.timeInMillis = it
            val date =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarDate.time)
            binding.receiptDateInput.setText(date)
        }
    }
}