package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddStoreBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.NipValidator
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Store
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

    private var pickedCategory: Category? = null
    private var isUniqueNIP = false
    private var firstEntry = true

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

        consumeNavArgs()
        initObserver()
        addStoreDataViewModel.refreshStoreList()
        addStoreDataViewModel.refreshCategoryList()

        applyInputParameters()

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
                            saveChangesToDatabase()
                        }.show(childFragmentManager, "TAG")
                    } else {
                        saveChangesToDatabase()
                    }
                    true
                }

                else -> false
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )
        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }

        binding.storeDefaultCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            pickedCategory = adapter.getItemAtPosition(position) as Category

            if ("" == pickedCategory?.color && binding.storeDefaultCategoryInput.adapter.count - 1 == position) {
                binding.storeDefaultCategoryInput.setText("")

                val action = AddStoreFragmentDirections.actionAddStoreFragmentToAddCategoryFragment(
                    categoryId = "",
                    inputType = AddingInputType.EMPTY.name,
                    sourceFragment = FragmentName.ADD_STORE_FRAGMENT
                )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                binding.storeDefaultCategoryInput.setText(pickedCategory?.name)
                binding.storeDefaultCategoryInput.isEnabled = false
            }
        }

        binding.storeDefaultCategoryLayout.setStartIconOnClickListener {
            binding.storeDefaultCategoryInput.setText("")
            pickedCategory = null
            binding.storeDefaultCategoryInput.isEnabled = true
        }
    }

    private fun consumeNavArgs() {
        if (firstEntry && navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
            firstEntry = false
            addStoreDataViewModel.inputType = navArgs.inputType
            addStoreDataViewModel.storeId = navArgs.storeId
            addStoreDataViewModel.storeName = navArgs.storeName
            addStoreDataViewModel.storeNip = navArgs.storeNip
            if (navArgs.categoryId.isNotEmpty()) {
                addStoreDataViewModel.categoryId = navArgs.categoryId
            }
        } else if (navArgs.sourceFragment == FragmentName.ADD_CATEGORY_FRAGMENT) {
            if (navArgs.categoryId.isNotEmpty()) {
                addStoreDataViewModel.categoryId = navArgs.categoryId
            }
        }
    }

    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addStoreDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addStoreDataViewModel.storeById.value = null
            mode = DataMode.NEW
            binding.storeNIPInput.setText("")
            binding.storeNameInput.setText("")
            binding.storeDefaultCategoryInput.setText("")
            binding.toolbar.title = getString(R.string.new_store_title)

        } else if (inputType == AddingInputType.ID) {
            if (addStoreDataViewModel.storeId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_store_title)
                mode = DataMode.EDIT
                addStoreDataViewModel.getStoreById(addStoreDataViewModel.storeId)
            } else {
                throw Exception("NO STORE ID SET")
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
        if (!NipValidator.validate(text)) {
            binding.storeNIPLayout.error = getString(R.string.nip_incorrect)
            binding.storeNIPLayout.helperText = null
        } else {
            binding.storeNIPLayout.error = null
            binding.storeNIPLayout.helperText = getString(R.string.nip_correct)
        }
    }

    private fun saveChangesToDatabase() {
        if (mode == DataMode.NEW) {
            val store = Store(
                binding.storeNIPInput.text.toString(),
                binding.storeNameInput.text.toString(),
                pickedCategory?.id ?: throw NoCategoryIdException()
            )
            addStoreDataViewModel.insertStore(store)
        }
        if (mode == DataMode.EDIT) {
            val store = addStoreDataViewModel.storeById.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId = pickedCategory?.id ?: throw NoCategoryIdException()
            addStoreDataViewModel.updateStore(store)
        }
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
            if (addStoreDataViewModel.categoryId.isNotEmpty()) {
                setCategory()
            }

        }

        addStoreDataViewModel.storeById.observe(viewLifecycleOwner) {
            it?.let { store ->
                if (navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
                    addStoreDataViewModel.categoryId = store.defaultCategoryId
                    setCategory()
                }
                binding.storeNameInput.setText(store.name)
                binding.storeNIPInput.setText(store.nip)

                validateNip(store.nip)
            }
        }

        addStoreDataViewModel.savedStore.observe(viewLifecycleOwner) {
            it?.let {
                //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
                listingViewModel.loadDataByStoreFilter()
                listingViewModel.loadDataByReceiptFilter()
                if (navArgs.sourceFragment == FragmentName.ADD_RECEIPT_FRAGMENT) {

                    val action =
                        AddStoreFragmentDirections.actionAddStoreFragmentToAddReceiptFragment(
                            inputType = AddingInputType.ID.name,
                            receiptId = "",
                            storeId = it.id,
                            sourceFragment = FragmentName.ADD_STORE_FRAGMENT
                        )
                    Navigation.findNavController(requireView()).navigate(action)
                } else {
                    Navigation.findNavController(requireView()).popBackStack()
                }
                addStoreDataViewModel.savedStore.value = null
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

    private fun clearInputs() {
        addStoreDataViewModel.storeById.value = null
    }

    private fun isStoreInputValid(): Boolean {
        if (binding.storeNameInput.text.toString() == "") {
            Toast.makeText(requireContext(), getString(R.string.empty_name), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (binding.storeNIPInput.text.toString() == "") {
            Toast.makeText(requireContext(), getString(R.string.empty_nip), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (pickedCategory?.id == null || pickedCategory?.id?.isEmpty() == true) {
            Toast.makeText(requireContext(), getString(R.string.pick_category), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!isNIPUnique(binding.storeNIPInput.text.toString())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.nip_already_exists),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun setCategory() {
        pickedCategory = try {
            addStoreDataViewModel.categoryList.value?.first { category -> category.id == addStoreDataViewModel.categoryId }
        } catch (exception: Exception) {
            null
        }
        pickedCategory?.let {
            binding.storeDefaultCategoryInput.setText(it.name)
            binding.storeDefaultCategoryInput.isEnabled = false
        }
    }
}
