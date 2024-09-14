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
import com.example.navsample.R
import com.example.navsample.adapters.StoreDropdownAdapter
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.exception.NoStoreIdException
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
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.reorder).setVisible(false)

        initObserver()

        binding.storeNIPInput.isEnabled = false

        receiptDataViewModel.refreshStoreList()
        receiptImageViewModel.bitmap.value?.let {
            binding.receiptImageMarked.setImageBitmap(it)
        }


        if (receiptDataViewModel.receipt.value == null) {
            changeViewToNewMode()
        }
        receiptDataViewModel.store.value?.let {
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

    private fun setReceiptDataToInputs(receipt: Receipt) {
        binding.receiptPTUInput.setText(receipt.ptu.toString())
        binding.receiptPLNInput.setText(receipt.pln.toString())
        binding.receiptDateInput.setText(receipt.date)
        binding.receiptTimeInput.setText(receipt.time)
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

                    val indexOfStore = storeList.map { it.nip }.indexOf(it.nip)
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
        receiptDataViewModel.store.observe(viewLifecycleOwner) {
            it?.let {
                setStoreDataToInputs(it)
            }
            pickedStore = it
        }
        receiptDataViewModel.receipt.observe(viewLifecycleOwner) {
            it?.let {
                setReceiptDataToInputs(it)
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

    private fun changeViewToNewMode() {
        binding.toolbar.title = "New receipt"
        binding.toolbar.menu.findItem(R.id.confirm).setVisible(true)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).setVisible(false)
        binding.toolbar.menu.findItem(R.id.add_new).setVisible(false)
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
        binding.toolbar.menu.findItem(R.id.confirm).setVisible(false)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).setVisible(true)
        binding.toolbar.menu.findItem(R.id.add_new).setVisible(true)
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
        binding.toolbar.menu.findItem(R.id.confirm).setVisible(true)
        binding.toolbar.setNavigationIcon(R.drawable.clear)
        binding.toolbar.menu.findItem(R.id.edit).setVisible(false)
        binding.toolbar.menu.findItem(R.id.add_new).setVisible(false)
    }

    private fun saveChangesToDatabase() {
        if (pickedStore == null) {
            Toast.makeText(requireContext(), "Pick store!", Toast.LENGTH_SHORT).show()
            return
        }
        if (mode == DataMode.NEW) {
            val receipt = Receipt(-1, -1.0, -1.0, "", "").apply {
                storeId = pickedStore!!.id!!
                pln = transformToDouble(binding.receiptPLNInput.text.toString())
                ptu = transformToDouble(binding.receiptPTUInput.text.toString())
                date = binding.receiptDateInput.text.toString()
                time = binding.receiptTimeInput.text.toString()
            }
            receiptDataViewModel.insertReceipt(receipt)
        } else if (mode == DataMode.EDIT) {
            receiptDataViewModel.receipt.value?.let {
                val receipt = Receipt(-1, -1.0, -1.0, "", "")
                receipt.id = it.id
                receipt.storeId = pickedStore!!.id!!
                receipt.pln = transformToDouble(binding.receiptPLNInput.text.toString())
                receipt.ptu = transformToDouble(binding.receiptPTUInput.text.toString())
                receipt.date = binding.receiptDateInput.text.toString()
                receipt.time = binding.receiptTimeInput.text.toString()
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
        binding.storeNIPLayout.visibility = View.GONE
        changeViewToDisplayMode()

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
            receiptDataViewModel.savedStore.value = null
            Navigation.findNavController(it)
                .navigate(R.id.action_addReceiptFragment_to_editStoreFragment)
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit -> {
                    binding.storeNIPLayout.visibility = View.VISIBLE
                    changeViewToEditMode()
                    return@setOnMenuItemClickListener true
                }

                R.id.confirm -> {
                    saveChangesToDatabase()
                    return@setOnMenuItemClickListener true
                }

                R.id.confirm -> {
                    saveChangesToDatabase()
                    return@setOnMenuItemClickListener true
                }

                R.id.add_new -> {
                    receiptDataViewModel.receipt.value?.id?.let {
                        receiptDataViewModel.refreshProductListForReceipt(it)
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
                binding.storeNIPInput.setText(receiptDataViewModel.store.value?.nip)
                binding.receiptPTUInput.setText(receiptDataViewModel.receipt.value?.ptu.toString())
                binding.receiptPLNInput.setText(receiptDataViewModel.receipt.value?.pln.toString())
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

        binding.storeNIPLayout.setStartIconOnClickListener {
            binding.storeNIPInput.text
            val index = receiptDataViewModel.storeList.value?.map { it.nip }
                ?.indexOf(binding.storeNIPInput.text.toString()) ?: -1
            if (index >= 0) {
                pickedStore = receiptDataViewModel.storeList.value?.get(index)
                binding.storeNIPInput.setText(pickedStore?.nip ?: "")
                binding.storeNameInput.setText(pickedStore?.name ?: "")
                binding.storeNameInput.isEnabled = false
                binding.toolbar.menu.findItem(R.id.confirm).setVisible(true)
                binding.toolbar.menu.findItem(R.id.edit).setVisible(false)
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