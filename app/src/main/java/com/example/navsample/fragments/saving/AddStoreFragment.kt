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
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddStoreBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.Store
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddStoreDataViewModel

class AddStoreFragment : Fragment() {
    private var _binding: FragmentAddStoreBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddStoreFragmentArgs by navArgs()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addStoreDataViewModel: AddStoreDataViewModel by activityViewModels()

    private var mode = DataMode.NEW

    private var chosenCategory: Category? = null
    private var isUniqueNIP = false
    private var firstEntry = true
    private var stateAfterSave = false

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

        dropdownAdapter = CategoryDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeDefaultCategoryInput.setAdapter(adapter)
        }

        addStoreDataViewModel.storeById.value = null
        initObserver()
        addStoreDataViewModel.refreshStoreList()
        addStoreDataViewModel.refreshCategoryList()


        if (firstEntry) {
            firstEntry = false
            addStoreDataViewModel.inputType = navArgs.inputType
            addStoreDataViewModel.storeId = navArgs.storeId
            addStoreDataViewModel.storeName = navArgs.storeName
            addStoreDataViewModel.storeNip = navArgs.storeNip
        }
        consumeNavArgs()



        imageViewModel.bitmapCroppedReceipt.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }

        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            validateNip(text.toString())
            isUniqueNIP = isNIPUnique(text.toString())
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!isStoreInputValid()) {
                        return@setOnMenuItemClickListener false
                    }

                    if (binding.storeNIPLayout.error != null) {
                        ConfirmDialog("Invalid NIP", "Continue?")
                        {
                            stateAfterSave = true
                            saveChangesToDatabase()
                        }.show(childFragmentManager, "TAG")
                    } else {
                        stateAfterSave = true
                        saveChangesToDatabase()
                    }
                    true
                }

                else -> false
            }
        }


        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.storeDefaultCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            chosenCategory = adapter.getItemAtPosition(position) as Category

            if ("" == chosenCategory?.color && binding.storeDefaultCategoryInput.adapter.count - 1 == position) {
                binding.storeDefaultCategoryInput.setText("")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_addStoreFragment_to_addCategoryFragment)
            } else {
                binding.storeDefaultCategoryInput.setText(chosenCategory?.name)
                binding.storeDefaultCategoryInput.isEnabled = false
            }
        }

        binding.storeDefaultCategoryLayout.setStartIconOnClickListener {
            binding.storeDefaultCategoryInput.setText("")
            chosenCategory = null
            binding.storeDefaultCategoryInput.isEnabled = true
        }
    }

    private fun consumeNavArgs() {
        val inputType = AddingInputType.getByName(addStoreDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            mode = DataMode.NEW
            binding.storeNIPInput.setText("")
            binding.storeNameInput.setText("")
            binding.storeDefaultCategoryInput.setText("")
            binding.toolbar.title = "Add store"

        } else if (inputType == AddingInputType.ID) {
            if (addStoreDataViewModel.storeId >= 0) {
                binding.toolbar.title = "Edit store"
                mode = DataMode.EDIT
                addStoreDataViewModel.getStoreById(addStoreDataViewModel.storeId)
            } else {
                throw Exception("NO STORE ID SET: " + addStoreDataViewModel.storeId)
            }
        } else if (inputType == AddingInputType.FIELD) {
            binding.storeNameInput.setText(addStoreDataViewModel.storeName)
            binding.storeNIPInput.setText(addStoreDataViewModel.storeNip)
            binding.storeDefaultCategoryInput.setText("")
        } else {
            throw Exception("BAD INPUT TYPE: " + addStoreDataViewModel.inputType)
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
            val store = Store("", "", null)
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId = chosenCategory?.id ?: throw NoCategoryIdException()
            addStoreDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = addStoreDataViewModel.storeById.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId = chosenCategory?.id ?: throw NoCategoryIdException()
            addStoreDataViewModel.updateStore(store)
        }
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
        imageViewModel.bitmapCroppedReceipt.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(imageViewModel.bitmapCroppedReceipt.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }

        addStoreDataViewModel.categoryList.observe(viewLifecycleOwner) { categoryList ->
            categoryList?.let {
                dropdownAdapter.categoryList = it
                dropdownAdapter.notifyDataSetChanged()
            }

        }

        addStoreDataViewModel.storeById.observe(viewLifecycleOwner) {
            it?.let { store ->
                val category = try {
                    addStoreDataViewModel.categoryList.value?.first { category -> category.id == store.defaultCategoryId }
                } catch (e: Exception) {
                    null
                }
                chosenCategory =
                    category ?: (addStoreDataViewModel.categoryList.value?.get(0) ?: Category(
                        "",
                        ""
                    ))

                binding.storeNameInput.setText(store.name)
                binding.storeNIPInput.setText(store.nip)
                binding.storeDefaultCategoryInput.setText(chosenCategory?.name)
                binding.storeDefaultCategoryInput.isEnabled = false

                validateNip(store.nip)

            }
        }

        addStoreDataViewModel.savedStore.observe(viewLifecycleOwner) {
            if (!stateAfterSave) {
                return@observe
            }
            stateAfterSave = false
            //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
            listingViewModel.loadDataByStoreFilter()
            listingViewModel.loadDataByReceiptFilter()
            if (navArgs.sourceFragment == "AddReceiptFragment") {

                val action =
                    AddStoreFragmentDirections.actionAddStoreFragmentToAddReceiptFragment(
                        inputType = AddingInputType.ID.name,
                        receiptId = -2,
                        storeId = it.id ?: -1,
                        sourceFragment = FRAGMENT_NAME
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                Navigation.findNavController(requireView()).popBackStack()
            }
        }
    }

    private fun isNIPUnique(text: String): Boolean {
        if (addStoreDataViewModel.storeById.value?.nip == text) {
            return true
        }
        val index = addStoreDataViewModel.storeList.value?.map { it.nip }?.indexOf(text) ?: -1
        if (addStoreDataViewModel.storeList.value?.find { it.nip == text } != null) {
            binding.storeNIPLayout.error =
                "NIP exist in store " + (addStoreDataViewModel.storeList.value?.get(index)?.name)
            return false
        }
        return true

    }

    private fun isStoreInputValid(): Boolean {
        if (binding.storeNameInput.text.toString() == "") {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.storeNIPInput.text.toString() == "") {
            Toast.makeText(requireContext(), "NIP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (chosenCategory?.id == null || chosenCategory?.id == -1) {
            Toast.makeText(requireContext(), "Category cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isUniqueNIP) {
            Toast.makeText(requireContext(), "NIP cannot be duplicated", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        const val FRAGMENT_NAME = "AddStoreFragment"
    }
}
