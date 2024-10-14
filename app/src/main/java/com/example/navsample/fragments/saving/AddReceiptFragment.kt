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
import com.example.navsample.R
import com.example.navsample.adapters.StoreDropdownAdapter
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.Utils.Companion.doubleToString
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddReceiptFragment : Fragment() {
    private var _binding: FragmentAddReceiptBinding? = null
    private val binding get() = _binding!!
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()


    private var calendarDate = Calendar.getInstance()
    private var calendarTime = Calendar.getInstance()
    private lateinit var dropdownAdapter: StoreDropdownAdapter
    private var mode = DataMode.NEW
    private var pickedStore: Store? = null
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

        initObserver()

        dropdownAdapter = StoreDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeNameInput.setAdapter(adapter)
        }

        receiptDataViewModel.refreshStoreList()
        receiptImageViewModel.bitmapCroppedReceipt.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }
        setStoreName(receiptDataViewModel.store.value, receiptDataViewModel.storeList.value)


        if (receiptDataViewModel.receipt.value == null) {
            changeViewToNewMode()
        }

        receiptDataViewModel.receipt.value?.let { receipt ->
            if ((receipt.id ?: -1) >= 0) {
                changeViewToDisplayMode()
                setReceiptDataToInputs(receipt)
            } else {
                changeViewToNewMode()
            }
        }
        defineClickListeners()
    }

    private fun setReceiptDataToInputs(receipt: Receipt) {
        binding.receiptPTUInput.setText(doubleToString(receipt.ptu))
        binding.receiptPLNInput.setText(doubleToString(receipt.pln))
        binding.receiptDateInput.setText(receipt.date)
        binding.receiptTimeInput.setText(receipt.time)
    }

    private fun setStoreName(store: Store?, storeList: ArrayList<Store>?) {
        if (store == null) {
            binding.storeNameInput.setText("")
            return
        }
        if (storeList.isNullOrEmpty()) {
            binding.storeNameInput.setText(store.name)
            return
        }

        val indexOfStore = storeList.map { sort -> sort.nip }.indexOf(store.nip)
        if (indexOfStore < 0) {
            binding.storeNameInput.setText("")
            Navigation.findNavController(requireView())
                .navigate(R.id.action_addReceiptFragment_to_editStoreFragment)
        } else {
            pickedStore = storeList[indexOfStore]
            binding.storeNameInput.setText(storeList[indexOfStore].name)
            binding.storeNameInput.isEnabled = false
        }
    }

    private fun initObserver() {
        receiptImageViewModel.bitmapCroppedReceipt.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(receiptImageViewModel.bitmapCroppedReceipt.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                dropdownAdapter.storeList = storeList
                dropdownAdapter.notifyDataSetChanged()
                setStoreName(receiptDataViewModel.store.value, storeList)
            }


        }
        receiptDataViewModel.store.observe(viewLifecycleOwner) {

            setStoreName(receiptDataViewModel.store.value, receiptDataViewModel.storeList.value)
            pickedStore = it
        }
        //TODO Reduce duplicated observed (methods above and under)
        receiptDataViewModel.savedStore.observe(viewLifecycleOwner) {

            setStoreName(
                receiptDataViewModel.savedStore.value,
                receiptDataViewModel.storeList.value
            )
            pickedStore = it
        }
        receiptDataViewModel.receipt.observe(viewLifecycleOwner) {
            it?.let {
                setReceiptDataToInputs(it)
            }
        }

        imageAnalyzerViewModel.receiptAnalyzed.observe(viewLifecycleOwner) {

            if (it == null) {
                return@observe
            }
            val store = Store(it.valueNIP, it.companyName, 0)
            val receipt = Receipt(
                -1,
                it.valuePLN.toString().toDouble(),
                it.valuePTU.toString().toDouble(),
                it.valueDate,
                it.valueTime
            )
            Log.i("ImageProcess", "valueNIP ${it.valueNIP}")
            Log.i("ImageProcess", "companyName ${it.companyName}")
            Log.i("ImageProcess", "valuePTU ${it.valuePTU}")
            Log.i("ImageProcess", "valuePLN ${it.valuePLN}")
            Log.i("ImageProcess", "valueDate ${it.valueDate}")
            Log.i("ImageProcess", "valueTime ${it.valueTime}")
            receiptDataViewModel.store.value = store
            receiptDataViewModel.receipt.value = receipt


        }
    }

    private fun transformToDouble(value: String): Double {
        return try {
            value.replace(",", ".").toDouble()
        } catch (t: Throwable) {
            0.0
        }
    }

    private fun changeViewToNewMode() {
        binding.toolbar.title = "New receipt"
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = false
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.storeNameLayout.isEnabled = false
        binding.receiptPLNLayout.isEnabled = false
        binding.receiptPTULayout.isEnabled = false
        binding.receiptPTUInput.isEnabled = false
        binding.receiptTimeLayout.isEnabled = false
        binding.receiptDateLayout.isEnabled = false

        binding.toolbar.title = "Receipt"
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = true
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = true
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.toolbar.title = "Edit receipt"
        binding.storeNameLayout.isEnabled = true
        binding.receiptPLNLayout.isEnabled = true
        binding.receiptPTULayout.isEnabled = true
        binding.receiptPTUInput.isEnabled = true
        binding.receiptTimeLayout.isEnabled = true
        binding.receiptDateLayout.isEnabled = true
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
        binding.toolbar.setNavigationIcon(R.drawable.clear)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false
        binding.toolbar.menu.findItem(R.id.add_new).isVisible = false
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

    private fun saveChangesToDatabase() {
        if (pickedStore == null) {
            Toast.makeText(requireContext(), "Pick store!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validateObligatoryFields()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val pln = transformToDouble(binding.receiptPLNInput.text.toString())
        val ptu = transformToDouble(binding.receiptPTUInput.text.toString())
        val date = binding.receiptDateInput.text.toString()
        val time = binding.receiptTimeInput.text.toString()


        if (pickedStore == null) {
            Toast.makeText(requireContext(), "Pick store!", Toast.LENGTH_SHORT).show()
            return
        }
        if (mode == DataMode.NEW) {
            val receipt = Receipt(-1, -1.0, -1.0, "", "").apply {
                this.storeId = pickedStore!!.id!!
                this.pln = pln
                this.ptu = ptu
                this.date = date
                this.time = time
            }
            receiptDataViewModel.insertReceipt(receipt)
            val action =
                AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment()
            Navigation.findNavController(requireView()).navigate(action)
        } else if (mode == DataMode.EDIT) {
            receiptDataViewModel.receipt.value?.let {
                val receipt = Receipt(-1, -1.0, -1.0, "", "").apply {
                    this.id = it.id
                    this.storeId = pickedStore!!.id!!
                    this.pln = pln
                    this.ptu = ptu
                    this.date = date
                    this.time = time
                }
                receiptDataViewModel.updateReceipt(receipt)
            }
        }
        receiptDataViewModel.store.value = pickedStore

        receiptDataViewModel.receipt.value?.let { receipt ->
            receiptDataViewModel.receipt.value = Receipt(
                pickedStore?.id ?: throw NoStoreIdException(),
                receipt.pln.toString().toDouble(),
                receipt.ptu.toString().toDouble(),
                receipt.date,
                receipt.time
            )
            receiptDataViewModel.receipt.value?.id = receipt.id
        }
        changeViewToDisplayMode()

    }


    private fun defineClickListeners() {

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit -> {
                    changeViewToEditMode()
                    return@setOnMenuItemClickListener true
                }

                R.id.confirm -> {
                    saveChangesToDatabase()
                    return@setOnMenuItemClickListener true
                }

                R.id.add_new -> {
                    receiptDataViewModel.receipt.value?.id?.let { id ->
                        receiptDataViewModel.refreshProductListForReceipt(id)
                    }
                    val action =
                        AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                    return@setOnMenuItemClickListener true
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            if (mode == DataMode.EDIT) {
                changeViewToDisplayMode()
                binding.receiptPTUInput.setText(doubleToString(receiptDataViewModel.receipt.value?.ptu))
                binding.receiptPLNInput.setText(doubleToString(receiptDataViewModel.receipt.value?.pln))
                binding.receiptDateInput.setText(receiptDataViewModel.receipt.value?.date)
                binding.receiptTimeInput.setText(receiptDataViewModel.receipt.value?.time)
            } else {
                Navigation.findNavController(it).popBackStack()
            }
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
                receiptDataViewModel.savedStore.value = null
                binding.storeNameInput.setText("")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addReceiptFragment_to_editStoreFragment)
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
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarDate.time)
            binding.receiptDateInput.setText(date)
        }
    }
}