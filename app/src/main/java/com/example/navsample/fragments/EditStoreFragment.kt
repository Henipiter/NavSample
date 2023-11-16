package com.example.navsample.fragments

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
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
        initObserver()

        receiptDataViewModel.refreshStoreList()
        var actualNIP=""

        if (args.storeIndex != -1) {
            val store = receiptDataViewModel.storeList.value?.get(args.storeIndex)
            binding.storeNameInput.setText(store?.name)
            binding.nipInput.setText(store?.nip)
            actualNIP = store?.nip.toString()
            changeViewToDisplayMode()
        } else {
            mode = DataMode.NEW
            binding.saveChangesButton.visibility = View.VISIBLE
            binding.cancelChangesButton.visibility = View.GONE
            binding.editButton.visibility = View.GONE
        }

        binding.nipInput.doOnTextChanged { text, _, _, _ ->
            val index = receiptDataViewModel.storeList.value?.map { it.nip }?.indexOf(text.toString()) ?: -1

            if (text?.length==10 &&  text.toString()!=actualNIP && index >= 0) {
                binding.nipLayout.error =
                    "NIP exist in store " + (receiptDataViewModel.storeList.value?.get(index)?.name
                        ?: "")
            } else {
                binding.nipLayout.error = null
            }
        }

        binding.editButton.setOnClickListener {
            changeViewToEditMode()
        }
        binding.saveChangesButton.setOnClickListener {
            if(binding.nipLayout.error != null){
                Toast.makeText(requireContext(), "Change NIP!", Toast.LENGTH_SHORT).show()
            }
            saveChangesToDatabase()
            changeViewToDisplayMode()
            receiptDataViewModel.savedStore.value?.nip = binding.nipInput.text.toString()
            receiptDataViewModel.savedStore.value?.name = binding.storeNameInput.text.toString()


        }
        binding.cancelChangesButton.setOnClickListener {
            changeViewToDisplayMode()
            binding.nipInput.setText(receiptDataViewModel.savedStore.value?.nip)
            binding.storeNameInput.setText(receiptDataViewModel.savedStore.value?.name)
        }

        receiptDataViewModel.refreshStoreList()

    }

    private fun initObserver() {

        receiptDataViewModel.insertErrorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                receiptDataViewModel.insertErrorMessage.value,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            val store = Store("", "")
            store.nip = binding.nipInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = receiptDataViewModel.savedStore.value!!
            store.nip = binding.nipInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            receiptDataViewModel.updateStore(store)
        }
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.storeNameLayout.isEnabled = false
        binding.nipLayout.isEnabled = false
        binding.saveChangesButton.visibility = View.GONE
        binding.cancelChangesButton.visibility = View.GONE
        binding.editButton.visibility = View.VISIBLE
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.storeNameLayout.isEnabled = true
        binding.nipLayout.isEnabled = true
        binding.saveChangesButton.visibility = View.VISIBLE
        binding.cancelChangesButton.visibility = View.VISIBLE
        binding.editButton.visibility = View.GONE
    }
}
