package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.CategoryDropdownAdapter
import com.example.navsample.databinding.FragmentAddStoreBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.inputs.StoreInputs
import com.example.navsample.exception.NoCategoryIdException
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddStoreDataViewModel

class AddStoreFragment : AddingFragment() {
    private var _binding: FragmentAddStoreBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddStoreFragmentArgs by navArgs()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addStoreDataViewModel: AddStoreDataViewModel by activityViewModels()

    private var firstEntry = true

    private lateinit var dropdownAdapter: CategoryDropdownAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun isMandatoryFieldCorrect(): Boolean {
        return binding.storeDefaultCategoryLayout.error == null &&
                binding.storeNameLayout.error == null
    }

    override fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields(getInputs())) {
                        if (isMandatoryFieldCorrect()) {
                            ConfirmDialog(
                                getString(R.string.nip_incorrect),
                                getString(R.string.should_continue)
                            ) { save() }.show(childFragmentManager, "TAG")
                        }
                        return@setOnMenuItemClickListener false
                    }
                    save()

                    true
                }

                else -> false
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }
    }

    private fun validateObligatoryFields(
        storeInputs: StoreInputs,
        validateId: Boolean = true
    ): Boolean {
        val errors = addStoreDataViewModel.validateObligatoryFields(storeInputs, validateId)
        binding.storeDefaultCategoryLayout.error = errors.categoryId
        binding.storeNameLayout.error = errors.name
        binding.storeNIPLayout.error = errors.nip
        return errors.isCorrect()
    }


    private fun getInputs(): StoreInputs {
        return StoreInputs(
            binding.storeNameInput.text,
            binding.storeNIPInput.text,
            addStoreDataViewModel.pickedCategory?.id
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineToolbar()

        dropdownAdapter = CategoryDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeDefaultCategoryInput.setAdapter(adapter)
        }

        consumeNavArgs()
        initObserver()
        addStoreDataViewModel.refreshStoreList()
        addStoreDataViewModel.refreshCategoryList()

        imageViewModel.bitmapCroppedReceipt.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }

        binding.storeNIPInput.doOnTextChanged { text, _, _, _ ->
            val errorMessage = addStoreDataViewModel.validateNip(text)
            binding.storeNIPLayout.error = errorMessage
        }
        binding.storeNameInput.doOnTextChanged { text, _, _, _ ->
            val errorMessage = addStoreDataViewModel.validateName(text)
            binding.storeNameLayout.error = errorMessage
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )

        binding.storeDefaultCategoryInput.setOnItemClickListener { adapter, _, position, _ ->
            addStoreDataViewModel.pickedCategory = adapter.getItemAtPosition(position) as Category

            if ("" == addStoreDataViewModel.pickedCategory?.color && binding.storeDefaultCategoryInput.adapter.count - 1 == position) {
                binding.storeDefaultCategoryInput.setText("")
                saveInputsToViewModel()
                val action = AddStoreFragmentDirections.actionAddStoreFragmentToAddCategoryFragment(
                    categoryId = "",
                    inputType = AddingInputType.EMPTY.name,
                    sourceFragment = FragmentName.ADD_STORE_FRAGMENT
                )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                binding.storeDefaultCategoryInput.setText(addStoreDataViewModel.pickedCategory?.name)
                binding.storeDefaultCategoryLayout.error = null
                binding.storeDefaultCategoryInput.isEnabled = false
            }
        }

        binding.storeDefaultCategoryLayout.setStartIconOnClickListener {
            binding.storeDefaultCategoryInput.setText("")
            addStoreDataViewModel.pickedCategory = null
            binding.storeDefaultCategoryInput.isEnabled = true
        }
    }

    private fun saveInputsToViewModel() {
        addStoreDataViewModel.storeInputs.nip = binding.storeNIPInput.text.toString()
        addStoreDataViewModel.storeInputs.name = binding.storeNameInput.text.toString()
    }

    private fun putInputsFromViewModel() {
        binding.storeNIPInput.setText(addStoreDataViewModel.storeInputs.nip)
        binding.storeNameInput.setText(addStoreDataViewModel.storeInputs.name)
    }

    override fun consumeNavArgs() {
        if (firstEntry && navArgs.sourceFragment != FragmentName.ADD_CATEGORY_FRAGMENT) {
            firstEntry = false
            addStoreDataViewModel.inputType = navArgs.inputType
            addStoreDataViewModel.storeId = navArgs.storeId
            addStoreDataViewModel.storeName = navArgs.storeName
            addStoreDataViewModel.storeNip = navArgs.storeNip
            if (navArgs.categoryId.isNotEmpty()) {
                addStoreDataViewModel.categoryId = navArgs.categoryId
            }
            applyInputParameters()
        } else if (navArgs.sourceFragment == FragmentName.ADD_CATEGORY_FRAGMENT) {
            putInputsFromViewModel()
            if (navArgs.categoryId.isNotEmpty()) {
                addStoreDataViewModel.categoryId = navArgs.categoryId
            }
        }
    }

    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addStoreDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addStoreDataViewModel.storeById.value = null
            addStoreDataViewModel.mode = DataMode.NEW
            binding.storeNIPInput.setText("")
            binding.storeNameInput.setText("")
            binding.storeDefaultCategoryInput.setText("")
            binding.toolbar.title = getString(R.string.new_store_title)

        } else if (inputType == AddingInputType.ID) {
            if (addStoreDataViewModel.storeId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_store_title)
                addStoreDataViewModel.mode = DataMode.EDIT
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

    override fun save() {
        if (addStoreDataViewModel.mode == DataMode.NEW) {
            val store = Store(
                binding.storeNIPInput.text.toString(),
                binding.storeNameInput.text.toString(),
                addStoreDataViewModel.pickedCategory?.id ?: throw NoCategoryIdException()
            )
            addStoreDataViewModel.insertStore(store)
        } else if (addStoreDataViewModel.mode == DataMode.EDIT) {
            val store = addStoreDataViewModel.storeById.value!!
            store.nip = binding.storeNIPInput.text.toString()
            store.name = binding.storeNameInput.text.toString()
            store.defaultCategoryId =
                addStoreDataViewModel.pickedCategory?.id ?: throw NoCategoryIdException()
            addStoreDataViewModel.updateStore(store)
        }
    }

    override fun initObserver() {
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

                validateObligatoryFields(getInputs())

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

    override fun clearInputs() {
        addStoreDataViewModel.storeById.value = null
        addStoreDataViewModel.pickedCategory = null
        addStoreDataViewModel.categoryId = ""
    }


    private fun setCategory() {
        addStoreDataViewModel.pickedCategory = try {
            addStoreDataViewModel.categoryList.value?.first { category -> category.id == addStoreDataViewModel.categoryId }
        } catch (exception: Exception) {
            null
        }
        addStoreDataViewModel.pickedCategory?.let {
            binding.storeDefaultCategoryInput.setText(it.name)
            binding.storeDefaultCategoryInput.isEnabled = false
            binding.storeDefaultCategoryLayout.error = null
        }
    }
}
