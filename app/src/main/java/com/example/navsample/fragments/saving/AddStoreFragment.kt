package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddStoreBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.entities.Category
import com.example.navsample.entities.Store
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel

class AddStoreFragment : Fragment() {
    private var _binding: FragmentAddStoreBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var mode = DataMode.NEW
    private var chosenCategory = Category("", "")
    private var currentStoreId = -1
    private var isDuplicatedNIP = false
    private lateinit var dropdownAdapter: CategoryDropdownAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false

        dropdownAdapter = CategoryDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeDefaultCategoryInput.setAdapter(adapter)
        }

        initObserver()
        receiptDataViewModel.refreshStoreList()
        receiptDataViewModel.refreshCategoryList()
        receiptImageViewModel.bitmap.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }
        var actualNIP = ""

        receiptDataViewModel.store.value?.let { store ->
            val category = try {
                receiptDataViewModel.categoryList.value?.first { it.id == store.defaultCategoryId }
            } catch (e: Exception) {
                null
            }
            chosenCategory =
                category ?: (receiptDataViewModel.categoryList.value?.get(0) ?: Category("", ""))

            binding.storeNameInput.setText(store.name)
            binding.storeNIPInput.setText(store.nip)
            binding.storeDefaultCategoryInput.setText(chosenCategory.name)
            actualNIP = store.nip

            validateNip(actualNIP)
        }
        receiptDataViewModel.store.value = null


        receiptDataViewModel.savedStore.value?.let { store ->
            if ((store.id ?: -1) >= 0) {
                changeViewToDisplayMode()
            } else {
                mode = DataMode.NEW
                binding.toolbar.title = "Add store"
                binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
                binding.toolbar.setNavigationIcon(R.drawable.back)
                binding.toolbar.menu.findItem(R.id.edit).isVisible = false
            }
        }

        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            val index =
                receiptDataViewModel.storeList.value?.map { it.nip }?.indexOf(text.toString()) ?: -1

            val duplicatedStore =
                receiptDataViewModel.storeList.value?.find { it.nip == text.toString() }
            if (text.toString() != actualNIP && duplicatedStore != null && duplicatedStore.id != currentStoreId) {
                binding.storeNIPLayout.error =
                    "NIP exist in store " + (receiptDataViewModel.storeList.value?.get(index)?.name
                        ?: "")
                isDuplicatedNIP = true
                return@doOnTextChanged
            } else {
                isDuplicatedNIP = false
            }

            validateNip(text.toString())
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit -> {
                    changeViewToEditMode()
                    true
                }

                R.id.confirm -> {
                    if (isDuplicatedNIP) {
                        Toast.makeText(
                            requireContext(),
                            "NIP cannot be duplicated",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnMenuItemClickListener false
                    }
                    if (binding.storeNIPInput.text.toString() == "") {
                        Toast.makeText(requireContext(), "NIP cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnMenuItemClickListener false
                    }
                    if (chosenCategory.id == null || chosenCategory.id == -1) {
                        Toast.makeText(
                            requireContext(),
                            "Category cannot be empty",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnMenuItemClickListener false

                    }
                    if (binding.storeNIPInput.text.toString() == "")
                        if (binding.storeNIPLayout.error != null) {
                            Toast.makeText(requireContext(), "Incorrect nip", Toast.LENGTH_SHORT)
                                .show()
                        }
                    saveChangesToDatabase()
                    changeViewToDisplayMode()
                    receiptDataViewModel.store.value?.nip = binding.storeNIPInput.text.toString()
                    receiptDataViewModel.store.value?.name = binding.storeNameInput.text.toString()
                    receiptDataViewModel.refreshStoreList()
                    Navigation.findNavController(requireView()).popBackStack()
                }

                else -> false
            }
        }



        binding.toolbar.setNavigationOnClickListener {
            if (mode == DataMode.EDIT) {
                changeViewToDisplayMode()
                receiptDataViewModel.savedStore.value?.let { savedStore ->
                    binding.storeNIPInput.setText(savedStore.nip)
                    binding.storeNameInput.setText(savedStore.name)
                    validateNip(savedStore.nip)
                }
            } else {
                Navigation.findNavController(it).popBackStack()
            }
        }

        binding.storeDefaultCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            chosenCategory = adapter.getItemAtPosition(position) as Category

            if ("" == chosenCategory.color && binding.storeDefaultCategoryInput.adapter.count - 1 == position) {
                receiptDataViewModel.savedCategory.value = null
                binding.storeDefaultCategoryInput.setText("")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addStoreFragment_to_addCategoryFragment)
            } else {
                binding.storeDefaultCategoryInput.setText(chosenCategory.name)
            }
        }

        binding.storeDefaultCategoryLayout.setStartIconOnClickListener {
            binding.storeDefaultCategoryInput.setText("")
            //TODO verify that
//            binding.storeDefaultCategoryLayout.helperText = null
//            binding.storeDefaultCategoryInput.isEnabled = true
            chosenCategory = Category("", "")
        }
    }

    private fun validateNip(text: String) {
        if (!isCorrectNIP(text)) {
            binding.storeNIPLayout.error = "Bad NIP"
            binding.storeNIPLayout.helperText = null
        } else {
            binding.storeNIPLayout.error = null
            binding.storeNIPLayout.helperText = "Correct NIP"
        }
    }

    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            val store = Store("", "", 0)
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId = chosenCategory.id ?: throw NoCategoryIdException()
            receiptDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = receiptDataViewModel.savedStore.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId = chosenCategory.id ?: throw NoCategoryIdException()
            receiptDataViewModel.updateStore(store)
        }
    }

    private fun changeViewToDisplayMode() {
        mode = DataMode.DISPLAY
        binding.storeNameLayout.isEnabled = false
        binding.storeNIPLayout.isEnabled = false
        binding.storeDefaultCategoryLayout.isEnabled = false
        binding.toolbar.title = "Store"
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = false
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = true
    }

    private fun changeViewToEditMode() {
        mode = DataMode.EDIT
        binding.storeNameLayout.isEnabled = true
        binding.storeNIPLayout.isEnabled = true
        binding.storeDefaultCategoryLayout.isEnabled = true
        binding.toolbar.title = "Edit store"
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true
        binding.toolbar.setNavigationIcon(R.drawable.clear)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false
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

    private fun initObserver() {
        receiptImageViewModel.bitmap.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(receiptImageViewModel.bitmap.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }

        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) { categoryList ->
            categoryList?.let {
                dropdownAdapter.categoryList = it
                dropdownAdapter.notifyDataSetChanged()
            }

        }
    }
}
