package com.example.navsample.fragments

import android.R
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.navsample.DTO.DataMode
import com.example.navsample.databinding.FragmentStageBasicInfoBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class StageBasicInfoFragment : Fragment() {
    private var _binding: FragmentStageBasicInfoBinding? = null
    private val binding get() = _binding!!
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()


    private var picker: TimePickerDialog? = null
    private var calendarDate = Calendar.getInstance()
    private var calendarTime = Calendar.getInstance()

    private var mode = DataMode.NEW

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStageBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun initObserver() {
        receiptImageViewModel.bitmap.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmap.value != null) {
                    binding.receiptImageMarked.setImageBitmap(receiptImageViewModel.bitmap.value)
                }
            }
        }
        receiptDataViewModel.storeList.observe(viewLifecycleOwner) {
            it?.let {
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item_1,
                    it
                ).also { adapter ->
                    binding.storeNameInput.setAdapter(adapter)
                }
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
        binding.storeNIPLayout.isEnabled = false
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
        binding.storeNIPLayout.isEnabled = true
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
            val store = Store("", "")
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = receiptDataViewModel.savedStore.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.updateStore(store)
        }

        val receipt = receiptDataViewModel.savedReceipt.value ?: Receipt("", -1F, -1F, "", "")
        receipt.nip = binding.storeNIPInput.text.toString()
        receipt.pln = transformToFloat(binding.receiptPLNInput.text.toString())
        receipt.ptu = transformToFloat(binding.receiptPTUInput.text.toString())
        receipt.date = binding.receiptDateInput.text.toString()
        receipt.time = binding.receiptTimeInput.text.toString()
        receiptDataViewModel.insertReceipt(receipt)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        binding.editButton.setOnClickListener {
            binding.editStores.visibility = View.GONE
            binding.storeNIPLayout.visibility = View.VISIBLE

            changeViewToEditMode()
        }
        binding.saveChangesButton.setOnClickListener {
            saveChangesToDatabase()

            binding.storeNIPLayout.visibility = View.GONE
            binding.editStores.visibility = View.VISIBLE
            changeViewToDisplayMode()
            receiptDataViewModel.receipt.value?.storeNIP = binding.storeNIPInput.text.toString()
            receiptDataViewModel.receipt.value?.receiptPTU =
                binding.receiptPTUInput.text.toString()
            receiptDataViewModel.receipt.value?.receiptPLN =
                binding.receiptPLNInput.text.toString()
            receiptDataViewModel.receipt.value?.receiptDate =
                binding.receiptDateInput.text.toString()
            receiptDataViewModel.receipt.value?.receiptTime =
                binding.receiptTimeInput.text.toString()

        }
        binding.cancelChangesButton.setOnClickListener {
            changeViewToDisplayMode()

            binding.storeNIPInput.setText(receiptDataViewModel.receipt.value?.storeNIP)
            binding.receiptPTUInput.setText(receiptDataViewModel.receipt.value?.receiptPTU)
            binding.receiptPLNInput.setText(receiptDataViewModel.receipt.value?.receiptPLN)
            binding.receiptDateInput.setText(receiptDataViewModel.receipt.value?.receiptDate)
            binding.receiptTimeInput.setText(receiptDataViewModel.receipt.value?.receiptTime)
        }

        receiptDataViewModel.refreshStoreList()
        if (receiptImageViewModel.bitmap.value != null) {
            binding.receiptImageMarked.setImageBitmap(receiptImageViewModel.bitmap.value)
        }

        if (receiptDataViewModel.receipt.value != null) {
            val receipt = receiptDataViewModel.receipt.value
            if ((receipt?.id ?: -1) >= 0) {
                changeViewToDisplayMode()
            } else {
                changeViewToNewMode()
            }

            if (!isCorrectNIP(receipt?.storeNIP)) {
                binding.storeNIPLayout.error = "Bad NIP"
                binding.storeNIPLayout.helperText = null
            } else {
                binding.storeNIPLayout.error = null
                binding.storeNIPLayout.helperText = "Correct NIP"
                lifecycleScope.launch {
                    val indexOfNIP = receiptDataViewModel.storeList.value?.map { it.nip }
                        ?.indexOf(receipt?.storeNIP) ?: -1
                    if (indexOfNIP >= 0) {
                        binding.storeNameInput.setText(
                            receiptDataViewModel.storeList.value?.get(indexOfNIP)?.name
                        )
                    } else {
                        binding.storeNameInput.setText(receipt?.storeName)
                    }
                }
            }

            binding.storeNIPInput.setText(receipt?.storeNIP)
            binding.receiptPTUInput.setText(receipt?.receiptPTU)
            binding.receiptPLNInput.setText(receipt?.receiptPLN)
            binding.receiptDateInput.setText(receipt?.receiptDate)
            binding.receiptTimeInput.setText(receipt?.receiptTime)

        } else {
            changeViewToNewMode()
        }

        binding.storeNameInput.doOnTextChanged { actual, _, _, _ ->
            val storeLists = receiptDataViewModel.storeList.value?.map { it.name }
            if (storeLists?.contains(actual.toString()) == false) {
                binding.storeNameLayout.helperText = "New store will be added"
            } else {
                binding.storeNameLayout.helperText = null

            }
        }
        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            binding.storeNameLayout.helperText = null
        }

        binding.storeNameInput.setOnItemClickListener { adapter, _, i, _ ->
            val store = adapter.getItemAtPosition(i) as Store
            receiptDataViewModel.savedStore.value = store
            binding.storeNameInput.setText(store.name)
            binding.storeNIPInput.setText(store.nip)

        }
        binding.addProductsButton.setOnClickListener {


            val action =
                StageBasicInfoFragmentDirections.actionStageBasicInfoFragmentToShopListFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.storeNIPInput.doOnTextChanged { actual, _, _, _ ->

            if (!isCorrectNIP(actual.toString())) {
                binding.storeNIPLayout.error = "Bad NIP"
                binding.storeNIPLayout.helperText = null
            } else {
                binding.storeNIPLayout.error = null
                binding.storeNIPLayout.helperText = "Correct NIP"
            }

        }



        binding.receiptDateLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.receiptTimeLayout.setEndIconOnClickListener {
            showTimePicker()
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

        binding.receiptTimeInput.setText("$hourValue:$minuteValue")
    }

    private fun isCorrectNIP(valueNIP: String?): Boolean {
        if (valueNIP == null || !Regex("""[0-9]{10}""").matches(valueNIP)) {
            return false
        }
        val weight = arrayOf(6, 5, 7, 2, 3, 4, 5, 6, 7)
        var sum = 0
        for (i in 0..8) {
            sum += valueNIP[i].digitToInt() * weight[i]
        }
        return sum % 11 == valueNIP[9].digitToInt()
    }

    private fun showTimePicker() {

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendarTime.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendarTime.get(Calendar.MINUTE))
                .build()

        timePicker.show(childFragmentManager, "Test")
        timePicker.addOnPositiveButtonClickListener {
            calendarTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendarTime.set(Calendar.MINUTE, timePicker.minute)
            val time = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.receiptTimeInput.setText(time)
        }
    }

    private fun showDatePicker() {

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(calendarDate.timeInMillis)
                .build()
        datePicker.show(childFragmentManager, "Test")

        datePicker.addOnPositiveButtonClickListener {
            calendarDate.timeInMillis = it
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(calendarDate.time)
            binding.receiptDateInput.setText(date)
        }
    }
}