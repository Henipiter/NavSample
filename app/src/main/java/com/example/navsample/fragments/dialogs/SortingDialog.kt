package com.example.navsample.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.navsample.databinding.DialogSortingBinding
import com.example.navsample.dto.sort.Direction


class SortingDialog(
    private var options: List<String>,
    private var onFinish: (String, Direction) -> Unit
) : DialogFragment() {

    private var propertyButtonList = arrayListOf<RadioButton>()

    private var _binding: DialogSortingBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogSortingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectRadioButtons()


        for (index in 0..propertyButtonList.lastIndex) {
            if (index < options.size) {
                propertyButtonList[index].visibility = View.VISIBLE
                propertyButtonList[index].text = options[index]
            } else {
                propertyButtonList[index].visibility = View.GONE
            }
        }

        binding.confirmButton.setOnClickListener {
            val chosenText = options[getIndexOfChosenProperty()]
            val isAscending = binding.ascendingButton.isChecked

            Toast.makeText(requireContext(), chosenText + isAscending, Toast.LENGTH_SHORT).show()
            onFinish.invoke(chosenText, Direction.getDirection(isAscending))
            dismiss()
        }

    }

    private fun getIndexOfChosenProperty(): Int {
        for (index in 0..propertyButtonList.lastIndex) {
            if (propertyButtonList[index].isChecked) {
                return index
            }
        }
        return 0
    }

    private fun collectRadioButtons() {
        propertyButtonList = arrayListOf()
        propertyButtonList.add(binding.firstButton)
        propertyButtonList.add(binding.secondButton)
        propertyButtonList.add(binding.thirdButton)
        propertyButtonList.add(binding.fourthButton)
    }
}
