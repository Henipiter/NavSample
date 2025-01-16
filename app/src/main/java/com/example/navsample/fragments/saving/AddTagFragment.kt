package com.example.navsample.fragments.saving

import android.os.Bundle
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
import com.example.navsample.databinding.FragmentAddTagBinding
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.inputs.TagInputs
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddTagDataViewModel

class AddTagFragment : AddingFragment() {
    private var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!
    private val navArgs: AddTagFragmentArgs by navArgs()

    private val addTagDataViewModel: AddTagDataViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

    private var firstEntry = true
    private var stateAfterSave = false
    private var mode = DataMode.NEW

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getInputs(): TagInputs {
        return TagInputs(binding.tagNameInput.text)
    }

    override fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_basic_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.confirm).isVisible = true

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    if (!validateObligatoryFields(getInputs())) {
                        return@setOnMenuItemClickListener false
                    }

                    stateAfterSave = true
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineToolbar()

        initObserver()
        addTagDataViewModel.refreshTagList()
        consumeNavArgs()
        applyInputParameters()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    clearInputs()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        )
        binding.tagNameInput.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 0) {
                binding.tagNameLayout.error = getString(R.string.empty_value_error)
            } else if (text.toString() == addTagDataViewModel.tagById.value?.name) {
                binding.tagNameLayout.error = null
            } else if (addTagDataViewModel.tagList.value?.find { it.name == text.toString() } != null) {
                binding.tagNameLayout.error = getString(R.string.tag_already_exists)
            } else {
                binding.tagNameLayout.error = null
            }
        }
    }

    override fun clearInputs() {
        addTagDataViewModel.tagById.value = null
    }

    override fun consumeNavArgs() {
        if (firstEntry) {
            firstEntry = false
            addTagDataViewModel.inputType = navArgs.inputType
            addTagDataViewModel.tagId = navArgs.tagId
        }
    }

    private fun applyInputParameters() {
        val inputType = AddingInputType.getByName(addTagDataViewModel.inputType)
        if (inputType == AddingInputType.EMPTY) {
            addTagDataViewModel.tagById.value = null
            mode = DataMode.NEW
            binding.tagNameInput.setText("")
            binding.toolbar.title = getString(R.string.new_tag_title)

        } else if (inputType == AddingInputType.ID) {
            if (addTagDataViewModel.tagId.isNotEmpty()) {
                binding.toolbar.title = getString(R.string.edit_tag_title)
                mode = DataMode.EDIT
                addTagDataViewModel.getTagById(addTagDataViewModel.tagId)
            } else {
                throw Exception("NO TAG ID SET")
            }
        } else {
            throw Exception("BAD INPUT TYPE: " + addTagDataViewModel.inputType)
        }
    }


    override fun save() {
        if (mode == DataMode.NEW) {
            val tag = Tag(binding.tagNameInput.text.toString())
            addTagDataViewModel.insertTag(tag)
        }
        if (mode == DataMode.EDIT) {
            val tag = addTagDataViewModel.tagById.value!!
            tag.name = binding.tagNameInput.text.toString()
            addTagDataViewModel.updateTag(tag)
        }
    }

    private fun isTagInputValid(): Boolean {
        if (binding.tagNameLayout.error != null) {
            Toast.makeText(requireContext(), getString(R.string.bad_inputs), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun validateObligatoryFields(tagInputs: TagInputs): Boolean {
        val errors = addTagDataViewModel.validateObligatoryFields(tagInputs)
        binding.tagNameLayout.error = errors.name
        binding.tagNameLayout.errorIconDrawable = null
        return errors.isCorrect()
    }

    override fun initObserver() {
        addTagDataViewModel.tagById.observe(viewLifecycleOwner) {
            it?.let {
                binding.tagNameInput.setText(it.name)
            }
        }
        addTagDataViewModel.savedTag.observe(viewLifecycleOwner) {
            it?.let {
                //TODO zoptymalizować - odswiezać w zależnosci czy bylo dodane czy zupdatowane
                listingViewModel.loadDataByProductFilter()
                listingViewModel.loadDataByTagFilter()

                Navigation.findNavController(requireView()).popBackStack()
                addTagDataViewModel.savedTag.value = null
            }
        }
    }

}
