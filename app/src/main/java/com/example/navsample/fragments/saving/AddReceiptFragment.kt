package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.DTO.DataMode
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.R
import com.example.navsample.adapters.StoreDropdownAdapter
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
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
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()


    private var calendarDate = Calendar.getInstance()
    private var calendarTime = Calendar.getInstance()

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

        initObserver()

        binding.storeNIPInput.isEnabled = false

        receiptDataViewModel.refreshStoreList()
        receiptImageViewModel.bitmap.value?.let {
            binding.receiptImageMarked.setImageBitmap(it)
        }


        if (receiptDataViewModel.receipt.value == null) {
            changeViewToNewMode()
        }
        receiptDataViewModel.savedStore.value?.let {
            setStoreDataToInputs(it)
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

    private fun setStoreDataToInputs(store: Store) {
        binding.storeNIPInput.setText(store.nip)
        binding.storeNameInput.setText(store.name)
    }

    private fun setReceiptDataToInputs(receipt: ReceiptDTO) {
        binding.receiptPTUInput.setText(receipt.receiptPTU)
        binding.receiptPLNInput.setText(receipt.receiptPLN)
        binding.receiptDateInput.setText(receipt.receiptDate)
        binding.receiptTimeInput.setText(receipt.receiptTime)
    }

    private fun initObserver() {
        receiptImageViewModel.bitmap.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmap.value != null) {
                    binding.receiptImageMarked.setImageBitmap(receiptImageViewModel.bitmap.value)
                }
            }
        }
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                StoreDropdownAdapter(
                    requireContext(), R.layout.array_adapter_row, storeList
                ).also { adapter ->
                    binding.storeNameInput.setAdapter(adapter)
                }
            }
            if (storeList != null) {
                receiptDataViewModel.store.value?.let {
                    binding.storeNIPInput.setText(it.nip)
                    binding.storeNameInput.setText(it.name)

                    val readNIP = it.nip ?: ""
                    val indexOfStore = storeList.map { it.nip }.indexOf(readNIP)
                    if (indexOfStore < 0) {
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_addReceiptFragment_to_editStoreFragment)
                    } else {
                        pickedStore = storeList[indexOfStore]
                        binding.storeNameInput.setText(storeList[indexOfStore].name)
                        binding.storeNIPInput.setText(storeList[indexOfStore].nip)
                        binding.storeNameInput.isEnabled = false

                    }
                }
            }


        }
        receiptDataViewModel.savedStore.observe(viewLifecycleOwner) {
            it?.let {
                setStoreDataToInputs(it)
            }
        }
        receiptDataViewModel.receipt.observe(viewLifecycleOwner) {
            it?.let {
                setReceiptDataToInputs(it)
            }
        }
    }

    private fun transformToFloat(value: String): Float {
        return try {
            value.replace(",", ".").toFloat()
        } catch (t: Throwable) {
            0.0f
        }
    }

    private fun changeViewToNewMode() {
        binding.saveChangesButton.visibility = View.VISIBLE
        binding.cancelChangesButton.visibility = View.GONE
        binding.editButton.visibility = View.GONE
        binding.addProductsButton.isEnabled = false
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.storeNameLayout.isEnabled = false
        binding.receiptPLNLayout.isEnabled = false
        binding.receiptPTULayout.isEnabled = false
        binding.receiptTimeLayout.isEnabled = false
        binding.receiptDateLayout.isEnabled = false
        binding.saveChangesButton.visibility = View.GONE
        binding.cancelChangesButton.visibility = View.GONE
        binding.editButton.visibility = View.VISIBLE
        binding.addProductsButton.isEnabled = true
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.storeNameLayout.isEnabled = true
        binding.receiptPLNLayout.isEnabled = true
        binding.receiptPTULayout.isEnabled = true
        binding.receiptTimeLayout.isEnabled = true
        binding.receiptDateLayout.isEnabled = true
        binding.saveChangesButton.visibility = View.VISIBLE
        binding.cancelChangesButton.visibility = View.VISIBLE
        binding.editButton.visibility = View.GONE

        binding.addProductsButton.isEnabled = false
    }

    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            if (pickedStore == null) {
                Toast.makeText(requireContext(), "Pick store!", Toast.LENGTH_SHORT).show()
                return
            }
            val receipt = Receipt(-1, -1F, -1F, "", "").apply {
                storeId = pickedStore!!.id!!
                pln = transformToFloat(binding.receiptPLNInput.text.toString())
                ptu = transformToFloat(binding.receiptPTUInput.text.toString())
                date = binding.receiptDateInput.text.toString()
                time = binding.receiptTimeInput.text.toString()
            }
            receiptDataViewModel.insertReceipt(receipt)
        } else if (mode == DataMode.EDIT) {
            pickedStore = receiptDataViewModel.savedStore.value
            receiptDataViewModel.receipt.value?.let {
                val receipt = Receipt(-1, -1F, -1F, "", "")
                receipt.id = it.id
                receipt.storeId = pickedStore!!.id!!
                receipt.pln = transformToFloat(binding.receiptPLNInput.text.toString())
                receipt.ptu = transformToFloat(binding.receiptPTUInput.text.toString())
                receipt.date = binding.receiptDateInput.text.toString()
                receipt.time = binding.receiptTimeInput.text.toString()
                receiptDataViewModel.updateReceipt(receipt)

            }
        }
        pickedStore = receiptDataViewModel.savedStore.value


    }


    private fun defineClickListeners() {

        binding.storeNameInput.doOnTextChanged { actual, _, _, _ ->
            val storeLists = receiptDataViewModel.storeList.value?.map { it.name }
            if (storeLists?.contains(actual.toString()) == false) {
                binding.storeNameLayout.helperText = "New store will be added"
            } else {
                binding.storeNameLayout.helperText = null

            }
        }
        binding.storeNIPLayout.setEndIconOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_addReceiptFragment_to_editStoreFragment)
        }

        binding.editButton.setOnClickListener {
            binding.storeNIPLayout.visibility = View.VISIBLE

            changeViewToEditMode()
        }
        binding.saveChangesButton.setOnClickListener {
            saveChangesToDatabase()

            receiptDataViewModel.savedReceipt.value?.let { receipt ->
                val savedStore = receiptDataViewModel.savedStore.value ?: Store("", "")
                receiptDataViewModel.receipt.value = ReceiptDTO(
                    receipt.id,
                    savedStore.name,
                    savedStore.nip,
                    receipt.pln.toString(),
                    receipt.ptu.toString(),
                    receipt.date,
                    receipt.time
                )
            }
            binding.storeNIPLayout.visibility = View.GONE
            changeViewToDisplayMode()

        }
        binding.cancelChangesButton.setOnClickListener {
            changeViewToDisplayMode()

            binding.storeNIPInput.setText(receiptDataViewModel.receipt.value?.storeNIP)
            binding.receiptPTUInput.setText(receiptDataViewModel.receipt.value?.receiptPTU)
            binding.receiptPLNInput.setText(receiptDataViewModel.receipt.value?.receiptPLN)
            binding.receiptDateInput.setText(receiptDataViewModel.receipt.value?.receiptDate)
            binding.receiptTimeInput.setText(receiptDataViewModel.receipt.value?.receiptTime)
        }

        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            binding.storeNameLayout.helperText = null
            binding.storeNameInput.isEnabled = true
            binding.storeNIPInput.setText("")
            pickedStore = null
        }

        binding.storeNameInput.setOnItemClickListener { adapter, _, i, _ ->
            val store = adapter.getItemAtPosition(i) as Store
            pickedStore = store
            binding.storeNameInput.setText(store.name)
            binding.storeNIPInput.setText(store.nip)
            binding.storeNameInput.isEnabled = false

        }
        binding.addProductsButton.setOnClickListener {
            receiptDataViewModel.receipt.value?.id?.let {
                receiptDataViewModel.refreshProductListWithConversion(it)
            }

            val action =
                AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.storeNIPLayout.setStartIconOnClickListener {
            binding.storeNIPInput.text
            val index = receiptDataViewModel.storeList.value?.map { it.nip }
                ?.indexOf(binding.storeNIPInput.text.toString()) ?: -1
            if (index >= 0) {
                pickedStore = receiptDataViewModel.storeList.value?.get(index)
                binding.storeNIPInput.setText(pickedStore?.nip ?: "")
                binding.storeNameInput.setText(pickedStore?.name ?: "")
                binding.storeNameInput.isEnabled = false
                binding.saveChangesButton.isEnabled = true
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