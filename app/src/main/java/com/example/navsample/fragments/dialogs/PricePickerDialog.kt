package com.example.navsample.fragments.dialogs

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogPricePickerBinding


class PricePickerDialog(
    private var lowerPrice: String,
    private var higherPrice: String,
    private var onConfirmClick: (String, String) -> Unit,

    ) : DialogFragment() {

    private var _binding: DialogPricePickerBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = DialogPricePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWidthPercent(95)
        binding.lowerPriceInput.setText(lowerPrice)
        binding.higherPriceInput.setText(higherPrice)
        binding.lowerPriceInput.doOnTextChanged { text, _, _, _ ->
            val higherPrice = binding.higherPriceInput.text.toString()
            if (higherPrice != "" && text.toString() != "") {
                try {
                    if (higherPrice.toDouble() < text.toString().toDouble()) {
                        binding.lowerPriceLayout.error = "Price too high"
                        binding.higherPriceLayout.error = null
                    } else {
                        binding.lowerPriceLayout.error = null
                        binding.higherPriceLayout.error = null
                    }
                } catch (e: Exception) {
                    binding.lowerPriceLayout.error = "Cannot convert to Double"
                    binding.higherPriceLayout.error = null
                }
            }
        }
        binding.higherPriceInput.doOnTextChanged { text, _, _, _ ->
            val lowerPrice = binding.lowerPriceInput.text.toString()
            if (lowerPrice != "" && text.toString() != "") {
                try {
                    if (lowerPrice.toDouble() > text.toString().toDouble()) {
                        binding.higherPriceLayout.error = "Price too low"
                        binding.lowerPriceLayout.error = null
                    } else {
                        binding.lowerPriceLayout.error = null
                        binding.higherPriceLayout.error = null
                    }
                } catch (e: Exception) {
                    binding.lowerPriceLayout.error = null
                    binding.higherPriceLayout.error = "Cannot convert to Double"
                }
            }
        }
        binding.lowerPriceLayout.setStartIconOnClickListener {
            binding.lowerPriceInput.setText("")
            binding.lowerPriceLayout.error = null
            binding.higherPriceLayout.error = null
        }
        binding.higherPriceLayout.setStartIconOnClickListener {
            binding.higherPriceInput.setText("")
            binding.lowerPriceLayout.error = null
            binding.higherPriceLayout.error = null
        }

        binding.confirmButton.setOnClickListener {
            val lowerPrice = binding.lowerPriceInput.text.toString()
            val higherPrice = binding.higherPriceInput.text.toString()
            if (binding.lowerPriceLayout.error == null && binding.higherPriceLayout.error == null) {
                onConfirmClick.invoke(lowerPrice, higherPrice)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Incorrect input values", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }


    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toDouble() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
