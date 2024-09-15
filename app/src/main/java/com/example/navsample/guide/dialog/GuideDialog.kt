package com.example.navsample.guide.dialog

import android.os.Bundle
import android.view.Gravity
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
    private lateinit var texts: List<String>
    private lateinit var instructions: List<() -> Unit>

    constructor(iterator: Int, texts: List<String>, instructions: List<() -> Unit>) : this() {

        if (texts.size + 1 != instructions.size) {
            throw IllegalArgumentException("Text size: ${texts.size}, Instructions size ${instructions.size}")
        }
        this.iterator = iterator
        this.texts = texts
        this.instructions = instructions
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.textInput.text = texts[iterator]
        instructions[1].invoke()


        binding.previousButton.setOnClickListener {
            instructions[iterator].invoke()
            iterator = Integer.max(iterator - 1, 0)
            binding.textInput.text = texts[iterator]

        }
        binding.nextButton.setOnClickListener {
            instructions[iterator + 1].invoke()
            iterator = Integer.min(iterator + 1, texts.size - 1)
            binding.textInput.text = texts[iterator]
        }
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