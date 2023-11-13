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
import com.example.navsample.databinding.FragmentStageBasicInfoBinding
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
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
//                    it.map { it2->it2.name }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        val dao = ReceiptDatabase.getInstance(requireContext()).receiptDao

        receiptDataViewModel.refreshStoreList()
        if (receiptImageViewModel.bitmap.value != null) {
            binding.receiptImageMarked.setImageBitmap(receiptImageViewModel.bitmap.value)
        }

        if (receiptDataViewModel.receipt.value != null) {
            val receipt = receiptDataViewModel.receipt.value
            if (!isCorrectNIP(receipt?.storeNIP)) {
                binding.storeNIPLayout.error = "Bad NIP"
                binding.storeNIPLayout.helperText = null
            } else {
                binding.storeNIPLayout.error = null
                binding.storeNIPLayout.helperText = "Correct NIP"
                lifecycleScope.launch {

                    val storeDb = receipt?.storeNIP?.let { dao.getStore(it) }
                    if (storeDb != null) {
                        binding.storeNameInput.setText(storeDb.name)
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
            binding.storeNameInput.setText(store.name)
            binding.storeNIPInput.setText(store.nip)

        }
        binding.addProductsButton.setOnClickListener {
            val newStore = Store(
                binding.storeNIPInput.text.toString(),
                binding.storeNameInput.text.toString()
            )
            val newReceipt = Receipt(
                binding.storeNIPInput.text.toString(),
                transformToFloat(binding.receiptPLNInput.text.toString()),
                transformToFloat(binding.receiptPTUInput.text.toString()),
                binding.receiptDateInput.text.toString(),
                binding.receiptTimeInput.text.toString()
            )
            receiptDataViewModel.savedStore.value = newStore
            lifecycleScope.launch {
                dao.insertStore(newStore)
                val rowId = dao.insertReceipt(newReceipt)
                newReceipt.id = dao.getReceiptId(rowId)
            }
            receiptDataViewModel.savedReceipt.value = newReceipt

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