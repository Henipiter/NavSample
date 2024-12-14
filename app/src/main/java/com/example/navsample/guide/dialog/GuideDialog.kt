package com.example.navsample.guide.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogGuideBinding

class GuideDialog() : DialogFragment() {


    private var _binding: DialogGuideBinding? = null
    private val binding get() = _binding!!

    private var iterator: Int = 0
    private var textIndex: Int = 0
    private lateinit var verticalLevel: List<Int>
    private lateinit var texts: List<String>
    private lateinit var instructions: List<() -> Unit>

    private var isLastMoveForward = true

    constructor(
        iterator: Int,
        texts: List<String>,
        instructions: List<() -> Unit>,
        verticalLevel: List<Int>
    ) : this() {

        if (texts.size + 1 != instructions.size) {
            throw IllegalArgumentException("Text size: ${texts.size}, Instructions size ${instructions.size}")
        }
        this.iterator = iterator
        this.texts = texts
        this.instructions = instructions
        this.verticalLevel = verticalLevel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {

                instructions[0].invoke()
                true
            } else {
                false
            }
        }

        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.textInput.text = texts[0]
        instructions[1].invoke()
        editVerticalLevelOfDialog(verticalLevel[0])


        binding.previousButton.setOnClickListener {
            if (isLastMoveForward) {
                iterator = Integer.max(iterator - 1, 0)
            }
            isLastMoveForward = false
            instructions[iterator].invoke()
            if (iterator == 0) {
                dismiss()
            }
            iterator = Integer.max(iterator - 1, 0)

            textIndex = Integer.max(textIndex - 1, 0)
            binding.textInput.text = texts[textIndex]
            editVerticalLevelOfDialog(verticalLevel[textIndex])

        }
        binding.nextButton.setOnClickListener {
            if (!isLastMoveForward) {
                iterator = Integer.min(iterator + 1, texts.size - 1)
            }
            isLastMoveForward = true
            instructions[iterator + 1].invoke()
            if (iterator + 1 == instructions.lastIndex) {
                dismiss()
            }
            iterator = Integer.min(iterator + 1, texts.size - 1)

            textIndex = Integer.min(textIndex + 1, texts.size - 1)
            binding.textInput.text = texts[textIndex]
            editVerticalLevelOfDialog(verticalLevel[textIndex])


        }
    }

    private fun editVerticalLevelOfDialog(level: Int) {
        val window = dialog?.window
        val layoutParams = window?.attributes
        layoutParams?.y = level
        window?.attributes = layoutParams
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)

            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

            layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams.y = 3
            layoutParams.dimAmount = 0f
            window.attributes = layoutParams
        }
    }
}