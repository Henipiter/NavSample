package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.navsample.DTO.DataMode
import com.example.navsample.databinding.FragmentEditStoreBinding
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ReceiptDataViewModel

class EditStoreFragment : Fragment() {
    private var _binding: FragmentEditStoreBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var mode = DataMode.DISPLAY
    private val args: EditStoreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiptDataViewModel.refreshStoreList()
        var actualNIP = ""

        if (args.storeIndex != -1) {
            val store = receiptDataViewModel.storeList.value?.get(args.storeIndex)
            binding.storeNameInput.setText(store?.name)
            binding.storeNIPInput.setText(store?.nip)
            actualNIP = store?.nip.toString()
            changeViewToDisplayMode()
        } else {
            mode = DataMode.NEW
            binding.saveChangesButton.visibility = View.VISIBLE
            binding.cancelChangesButton.visibility = View.GONE
            binding.editButton.visibility = View.GONE
        }

        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            val index =
                receiptDataViewModel.storeList.value?.map { it.nip }?.indexOf(text.toString()) ?: -1

            if (text.toString() != actualNIP && index >= 0) {
                binding.storeNIPLayout.error =
                    "NIP exist in store " + (receiptDataViewModel.storeList.value?.get(index)?.name
                        ?: "")
                return@doOnTextChanged
            }

            if (!isCorrectNIP(text.toString())) {
                binding.storeNIPLayout.error = "Bad NIP"
                binding.storeNIPLayout.helperText = null
            } else {
                binding.storeNIPLayout.error = null
                binding.storeNIPLayout.helperText = "Correct NIP"
            }
        }

        binding.editButton.setOnClickListener {
            changeViewToEditMode()
        }
        binding.saveChangesButton.setOnClickListener {
            if (binding.storeNIPLayout.error != null) {
                Toast.makeText(requireContext(), "Change NIP!", Toast.LENGTH_SHORT).show()
            }
            saveChangesToDatabase()
            changeViewToDisplayMode()
            receiptDataViewModel.savedStore.value?.nip = binding.storeNIPInput.text.toString()
            receiptDataViewModel.savedStore.value?.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.refreshStoreList()


        }
        binding.cancelChangesButton.setOnClickListener {
            changeViewToDisplayMode()
            binding.storeNIPInput.setText(receiptDataViewModel.savedStore.value?.nip)
            binding.storeNameInput.setText(receiptDataViewModel.savedStore.value?.name)
        }

        receiptDataViewModel.refreshStoreList()

    }


    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            val store = Store("", "")
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = receiptDataViewModel.savedStore.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.updateStore(store)
        }
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.storeNameLayout.isEnabled = false
        binding.storeNIPLayout.isEnabled = false
        binding.saveChangesButton.visibility = View.GONE
        binding.cancelChangesButton.visibility = View.GONE
        binding.editButton.visibility = View.VISIBLE
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.storeNameLayout.isEnabled = true
        binding.storeNIPLayout.isEnabled = true
        binding.saveChangesButton.visibility = View.VISIBLE
        binding.cancelChangesButton.visibility = View.VISIBLE
        binding.editButton.visibility = View.GONE
    }

    private fun isCorrectNIP(valueNIP: String?): Boolean {
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
}
