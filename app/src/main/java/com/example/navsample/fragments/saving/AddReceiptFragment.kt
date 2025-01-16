package com.example.navsample.fragments.saving

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.StoreDropdownAdapter
import com.example.navsample.databinding.FragmentAddReceiptBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.inputs.ReceiptInputs
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddReceiptDataViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddReceiptFragment : AddingFragment() {
    private var _binding: FragmentAddReceiptBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddReceiptFragmentArgs by navArgs()
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val addReceiptDataViewModel: AddReceiptDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()


    private var calendarDate = Calendar.getInstance()
    private var calendarTime = Calendar.getInstance()
    private lateinit var dropdownAdapter: StoreDropdownAdapter

    private var goNext = true
    private var firstEntry = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineToolbar()


        dropdownAdapter = StoreDropdownAdapter(
            requireContext(), R.layout.array_adapter_row, arrayListOf()
        ).also { adapter ->
            binding.storeNameInput.setAdapter(adapter)
        }
        consumeNavArgs()

        initObserver()
        addReceiptDataViewModel.refreshStoreList()

        imageViewModel.bitmapCroppedReceipt.value?.let {
            binding.receiptImage.setImageBitmap(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )
        defineClickListeners()
    }

    override fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.importImage).isVisible = false
        binding.toolbar.menu.findItem(R.id.aiAssistant).isVisible = false
        binding.toolbar.menu.findItem(R.id.reorder).isVisible = false

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields(getInputs())) {
                        return@setOnMenuItemClickListener false
                    }
                    save()
                    true
                }

                R.id.add_new -> {
                    addReceiptDataViewModel.receiptById.value?.let { receipt ->
                        val action =
                            AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment(
                                receiptId = receipt.id,
                                storeId = receipt.storeId,
                                categoryId = addReceiptDataViewModel.pickedStore?.defaultCategoryId!!
                            )
                        Navigation.findNavController(requireView()).navigate(action)
                        return@setOnMenuItemClickListener true
                    }
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.receipt_not_set),
                        Toast.LENGTH_SHORT
                    )

                    return@setOnMenuItemClickListener true
                }

                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            clearInputs()
            Navigation.findNavController(it).popBackStack()
        }
    }

    override fun clearInputs() {
        addReceiptDataViewModel.receiptById.value = null
        addReceiptDataViewModel.pickedStore = null
        addReceiptDataViewModel.storeId = ""
    }

    override fun consumeNavArgs() {
        if (firstEntry && navArgs.sourceFragment != FragmentName.ADD_STORE_FRAGMENT) {
            firstEntry = false
            addReceiptDataViewModel.inputType = navArgs.inputType
            if (navArgs.receiptId.isNotEmpty()) {
                addReceiptDataViewModel.receiptId = navArgs.receiptId
            }
            applyInputParameters()
        } else if (navArgs.sourceFragment == FragmentName.ADD_STORE_FRAGMENT) {
            putInputsFromViewModel()
            if (navArgs.storeId.isNotEmpty()) {
                addReceiptDataViewModel.storeId = navArgs.storeId
            }
        }
    }

    private fun saveInputsToViewModel() {
        addReceiptDataViewModel.receiptInputs.date = binding.receiptDateInput.text.toString()
        addReceiptDataViewModel.receiptInputs.time = binding.receiptTimeInput.text.toString()
        addReceiptDataViewModel.receiptInputs.pln = binding.receiptPLNInput.text.toString().toInt()
        addReceiptDataViewModel.receiptInputs.ptu = binding.receiptPTUInput.text.toString().toInt()
    }

    private fun putInputsFromViewModel() {
        binding.receiptDateInput.setText(addReceiptDataViewModel.receiptInputs.date)
        binding.receiptTimeInput.setText(addReceiptDataViewModel.receiptInputs.time)
        binding.receiptPLNInput.setText(addReceiptDataViewModel.receiptInputs.pln.toString())
        binding.receiptPTUInput.setText(addReceiptDataViewModel.receiptInputs.ptu.toString())
    }

    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addReceiptDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addReceiptDataViewModel.receiptById.value = null
            addReceiptDataViewModel.mode = DataMode.NEW
            binding.storeNameInput.setText("")
            binding.receiptPLNInput.setText("")
            binding.receiptPTUInput.setText("")
            binding.receiptDateInput.setText("")
            binding.receiptTimeInput.setText("")
            binding.toolbar.title = getString(R.string.new_receipt_title)
            binding.toolbar.menu.findItem(R.id.add_new).isVisible = false

        } else if (inputType == AddingInputType.ID) {
            if (addReceiptDataViewModel.receiptId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_receipt_title)
                addReceiptDataViewModel.mode = DataMode.EDIT
                addReceiptDataViewModel.getReceiptById(addReceiptDataViewModel.receiptId)
                binding.toolbar.menu.findItem(R.id.add_new).isVisible = true
            } else {
                throw Exception("NO RECEIPT ID SET")
            }
        } else {
            throw Exception("BAD INPUT TYPE: " + addReceiptDataViewModel.inputType)
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
        addReceiptDataViewModel.storeList.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                dropdownAdapter.storeList = storeList
                dropdownAdapter.notifyDataSetChanged()
            }
            if (addReceiptDataViewModel.storeId.isNotEmpty()) {
                setStore()
            }

        }
        addReceiptDataViewModel.receiptById.observe(viewLifecycleOwner) {
            it?.let { receipt ->

                if (navArgs.sourceFragment != FragmentName.ADD_STORE_FRAGMENT) {
                    addReceiptDataViewModel.storeId = it.storeId
                    setStore()
                }
                binding.receiptPLNInput.setText(intPriceToString(receipt.pln))
                binding.receiptPTUInput.setText(intPriceToString(receipt.ptu))
                binding.receiptDateInput.setText(receipt.date)
                binding.receiptTimeInput.setText(receipt.time)

                validateObligatoryFields(getInputs())
            }
        }

        imageAnalyzerViewModel.receiptAnalyzed.observe(viewLifecycleOwner) { analyzedReceipt ->
            if (analyzedReceipt == null) {
                return@observe
            }
            Log.i("ImageProcess", "valueNIP ${analyzedReceipt.valueNIP}")
            Log.i("ImageProcess", "companyName ${analyzedReceipt.companyName}")
            Log.i("ImageProcess", "valuePTU ${analyzedReceipt.valuePTU}")
            Log.i("ImageProcess", "valuePLN ${analyzedReceipt.valuePLN}")
            Log.i("ImageProcess", "valueDate ${analyzedReceipt.valueDate}")
            Log.i("ImageProcess", "valueTime ${analyzedReceipt.valueTime}")
            imageAnalyzerViewModel.receiptAnalyzed.value = null

            binding.receiptPLNInput.setText(analyzedReceipt.valuePLN.toString())
            binding.receiptPTUInput.setText(analyzedReceipt.valuePTU.toString())
            binding.receiptDateInput.setText(analyzedReceipt.valueDate)
            binding.receiptTimeInput.setText(analyzedReceipt.valueTime)

            val foundStore =
                addReceiptDataViewModel.storeList.value?.find { it.nip == analyzedReceipt.valueNIP }
            if (foundStore == null) {
                binding.storeNameInput.setText("")
                saveInputsToViewModel()
                val action =
                    AddReceiptFragmentDirections.actionAddReceiptFragmentToAddStoreFragment(
                        inputType = AddingInputType.FIELD.name,
                        storeName = analyzedReceipt.companyName,
                        storeNip = analyzedReceipt.valueNIP,
                        sourceFragment = FragmentName.ADD_RECEIPT_FRAGMENT,
                        categoryId = "",
                        storeId = ""
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                addReceiptDataViewModel.pickedStore = foundStore
                binding.storeNameInput.setText(foundStore.name)
                binding.storeNameInput.isEnabled = false
            }


        }
        addReceiptDataViewModel.savedReceipt.observe(viewLifecycleOwner) {
            it?.let {
                addReceiptDataViewModel.receiptId = it.id
                addReceiptDataViewModel.inputType = AddingInputType.ID.name
                if (goNext && addReceiptDataViewModel.pickedStore?.defaultCategoryId != null) {
                    goNext = false
                    if (addReceiptDataViewModel.mode == DataMode.NEW) {
                        val action =
                            AddReceiptFragmentDirections.actionAddReceiptFragmentToAddProductListFragment(
                                receiptId = it.id,
                                storeId = it.storeId,
                                categoryId = addReceiptDataViewModel.pickedStore?.defaultCategoryId!!
                            )
                        Navigation.findNavController(requireView()).navigate(action)
                    } else {
                        Navigation.findNavController(requireView()).popBackStack()
                    }
                }
                addReceiptDataViewModel.savedReceipt.value = null
            }
        }
    }

    private fun validateObligatoryFields(receiptInputs: ReceiptInputs): Boolean {
        val errors = addReceiptDataViewModel.validateObligatoryFields(receiptInputs)
        binding.storeNameLayout.error = errors.storeId
        binding.receiptPLNLayout.error = errors.pln
        binding.receiptPTULayout.error = errors.ptu
        binding.receiptDateLayout.error = errors.date
        binding.receiptTimeLayout.error = errors.time
        binding.storeNameLayout.errorIconDrawable = null
        binding.receiptPLNLayout.errorIconDrawable = null
        binding.receiptPTULayout.errorIconDrawable = null
        binding.receiptDateLayout.errorIconDrawable = null
        binding.receiptTimeLayout.errorIconDrawable = null
        return errors.isCorrect()
    }

    private fun getInputs(): ReceiptInputs {
        return ReceiptInputs(
            addReceiptDataViewModel.pickedStore?.id,
            binding.receiptPLNInput.text,
            binding.receiptPTUInput.text,
            binding.receiptDateInput.text,
            binding.receiptTimeInput.text
        )
    }

    override fun save() {
        addReceiptDataViewModel.save(getInputs(), {
            //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
            listingViewModel.loadDataByReceiptFilter()
            listingViewModel.loadDataByProductFilter()
            goNext = true
        }, {
            listingViewModel.loadDataByReceiptFilter()
            goNext = true
        }
        )
    }

    private fun defineClickListeners() {
        binding.storeNameLayout.setStartIconOnClickListener {
            binding.storeNameInput.setText("")
            binding.storeNameInput.isEnabled = true
            addReceiptDataViewModel.pickedStore = null
        }

        binding.storeNameInput.setOnItemClickListener { adapter, _, position, _ ->
            val store = adapter.getItemAtPosition(position) as Store
            if ("" == store.nip && binding.storeNameInput.adapter.count - 1 == position) {
                binding.storeNameInput.setText("")
                saveInputsToViewModel()
                val action =
                    AddReceiptFragmentDirections.actionAddReceiptFragmentToAddStoreFragment(
                        storeName = null,
                        storeNip = null,
                        sourceFragment = FragmentName.ADD_RECEIPT_FRAGMENT,
                        storeId = "",
                        categoryId = ""
                    )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                addReceiptDataViewModel.pickedStore = store
                binding.storeNameInput.setText(store.name)
                binding.storeNameInput.isEnabled = false
            }

        }

        binding.receiptDateLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.receiptDateInput.setOnClickListener {
            showDatePicker()
        }
        binding.receiptTimeLayout.setEndIconOnClickListener {
            showTimePicker()
        }
        binding.receiptTimeInput.setOnClickListener {
            showTimePicker()
        }

        binding.receiptDateInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptDateLayout.error = null
            }
        }
        binding.receiptTimeInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptTimeInput.error = null
            }
        }
        binding.receiptPLNInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptPLNLayout.error = null
            }
        }
        binding.receiptPTUInput.doOnTextChanged { actual, _, _, _ ->
            if (!actual.isNullOrEmpty()) {
                binding.receiptPTULayout.error = null
            }
        }
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendarTime.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendarTime.get(Calendar.MINUTE)).build()

        timePicker.show(childFragmentManager, "Test")
        timePicker.addOnPositiveButtonClickListener {
            calendarTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendarTime.set(Calendar.MINUTE, timePicker.minute)
            val time = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.receiptTimeInput.setText(time)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(calendarDate.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")

        datePicker.addOnPositiveButtonClickListener {
            calendarDate.timeInMillis = it
            val date =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarDate.time)
            binding.receiptDateInput.setText(date)
        }
    }

    private fun setStore() {
        addReceiptDataViewModel.pickedStore =
            addReceiptDataViewModel.storeList.value?.find { it.id == addReceiptDataViewModel.storeId }

        addReceiptDataViewModel.receiptById.value?.let {
            it.storeId = addReceiptDataViewModel.storeId
        }
        binding.storeNameInput.setText(addReceiptDataViewModel.pickedStore?.name)
        binding.storeNameInput.isEnabled = false
    }
}