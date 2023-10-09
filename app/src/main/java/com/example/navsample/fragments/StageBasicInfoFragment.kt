package com.example.navsample.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.databinding.FragmentStageBasicInfoBinding
import java.util.Locale


class StageBasicInfoFragment : Fragment() {
    private var _binding: FragmentStageBasicInfoBinding? = null
    private val binding get() = _binding!!
    val args: StageBasicInfoFragmentArgs by navArgs()


    var picker: TimePickerDialog? = null
    var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStageBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.uri != null) {
            binding.receiptImageMarked.setImageURI(args.uri)
            binding.storeNameInput.setText(args.receipt?.storeName)
            binding.storeNIPInput.setText(args.receipt?.storeNIP)
            binding.receiptPTUInput.setText(args.receipt?.receiptPTU)
            binding.receiptPLNInput.setText(args.receipt?.receiptPLN)
            binding.receiptDateInput.setText(args.receipt?.receiptDate)
            binding.receiptTimeInput.setText(args.receipt?.receiptTime)

            if (!verifyNIP(args.receipt?.storeNIP)) {
                binding.storeNIPInputInfo.visibility = View.VISIBLE
            }

        }
        binding.addProductsButton.setOnClickListener {
            val action =
                StageBasicInfoFragmentDirections.actionStageBasicInfoFragmentToShopListFragment(
                    args.productList, args.uri
                )
            Navigation.findNavController(it).navigate(action)
        }
        binding.storeNIPInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!verifyNIP(args.receipt?.storeNIP)) {
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