package com.example.navsample.fragments.dialogs

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogEditTextBinding

class EditTextDialog(
    var text: String,
    private var onStartClick: () -> Unit,
    private var onEndClick: () -> Unit,
    private var returnChange: (String) -> Unit,
    private var showSideButtons: Boolean
) : DialogFragment() {

    constructor(text: String, returnChange: (String) -> Unit) : this(
        text,
        {},
        {},
        returnChange,
        false
    )

    constructor(
        text: String,
        onStartClick: () -> Unit,
        onEndClick: () -> Unit,
        returnChange: (String) -> Unit
    ) : this(text, onStartClick, onEndClick, returnChange, true)

    private var _binding: DialogEditTextBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setWidthPercent(95)
        super.onViewCreated(view, savedInstanceState)
        if (!showSideButtons) {
            binding.addRowAtBottomButton.visibility = View.GONE
            binding.addRowAtTopButton.visibility = View.GONE
        } else {
            binding.addRowAtBottomButton.visibility = View.VISIBLE
            binding.addRowAtTopButton.visibility = View.VISIBLE

            binding.addRowAtTopButton.setOnClickListener {
                onStartClick.invoke()
                dismiss()
            }
            binding.addRowAtBottomButton.setOnClickListener {
                onEndClick.invoke()
                dismiss()
            }

        }
        binding.textInput.setText(text)

        binding.confirmButton.setOnClickListener {
            returnChange.invoke(binding.textInput.text.toString())
            dismiss()
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
