package com.example.navsample.fragments

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import kotlinx.coroutines.launch
import java.util.Locale


class StageBasicInfoFragment : Fragment() {
    private var _binding: FragmentStageBasicInfoBinding? = null
    private val binding get() = _binding!!
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()


    private var picker: TimePickerDialog? = null
    private var calendar = Calendar.getInstance()

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
    private fun transformToFloat(value:String):Float {
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
            if (!verifyNIP(receipt?.storeNIP)) {
                binding.storeNIPInputInfo.visibility = View.VISIBLE
            } else {
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

            if (!verifyNIP(receipt?.storeNIP)) {
                binding.storeNIPInputInfo.visibility = View.VISIBLE
            }

        }
        binding.storeNameInput.setOnItemClickListener{ adapter, _, i, _ ->
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
        binding.storeNIPInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!verifyNIP(receiptDataViewModel.receipt.value?.storeNIP)) {
                    binding.storeNIPInputInfo.visibility = View.VISIBLE
                } else {
                    binding.storeNIPInputInfo.visibility = View.INVISIBLE
                }
            }
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

    private fun verifyNIP(valueNIP: String?): Boolean {
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

    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.receiptDateInput.setText(sdf.format(calendar.time))
    }
}