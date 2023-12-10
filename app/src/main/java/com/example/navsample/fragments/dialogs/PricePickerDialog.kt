package com.example.navsample.fragments.dialogs

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogPricePickerBinding


class PricePickerDialog(
    var lowerPrice: String,
    var higherPrice: String,
    var onConfirmClick: (String, String) -> Unit,

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
            if (higherPrice != "") {
                if (higherPrice.toFloat() < text.toString().toFloat()) {
                    binding.lowerPriceLayout.error = "is to high"
                    binding.higherPriceLayout.error = null
                } else {
                    binding.lowerPriceLayout.error = null
                    binding.higherPriceLayout.error = null
                }
            }
        }
        binding.higherPriceInput.doOnTextChanged { text, _, _, _ ->
            val lowerPrice = binding.lowerPriceInput.text.toString()
            if (lowerPrice != "") {
                if (lowerPrice.toFloat() > text.toString().toFloat()) {
                    binding.higherPriceLayout.error = "is to low"
                    binding.lowerPriceLayout.error = null
                } else {
                    binding.lowerPriceLayout.error = null
                    binding.higherPriceLayout.error = null
                }
            }
        }
        binding.lowerPriceLayout.setStartIconOnClickListener {
            binding.lowerPriceInput.setText("")
        }
        binding.higherPriceLayout.setStartIconOnClickListener {
            binding.higherPriceInput.setText("")
        }

        binding.confirmButton.setOnClickListener {
            val lowerPrice = binding.lowerPriceInput.text.toString()
            val higherPrice = binding.higherPriceInput.text.toString()
            if (lowerPrice != "" && higherPrice != "" && lowerPrice.toFloat() <= higherPrice.toFloat()
            ) {
                onConfirmClick.invoke(lowerPrice, higherPrice)
                dismiss()
            }
        }
        binding.cancelButton.setOnClickListener { dismiss() }

    }


    fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}