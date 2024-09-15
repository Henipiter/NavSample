package com.example.navsample.guide.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogGuideBinding

class GuideDialog(
    var text: String,
    var onStartClick: () -> Unit,
    var onEndClick: () -> Unit
) : DialogFragment() {


    private var _binding: DialogGuideBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInput.text = text


        binding.previousButton.setOnClickListener {
            onStartClick.invoke()

        }
        binding.nextButton.setOnClickListener {
            onEndClick.invoke()
        }

    }
}