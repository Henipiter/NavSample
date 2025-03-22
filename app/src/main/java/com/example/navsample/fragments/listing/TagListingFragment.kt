package com.example.navsample.fragments.listing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.TagListAdapter
import com.example.navsample.databinding.FragmentTagListingBinding
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.database.Tag
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.sheets.TagBottomSheetFragment
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddTagDataViewModel

class TagListingFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentTagListingBinding? = null
    private val binding get() = _binding!!

    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addTagDataViewModel: AddTagDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var tagListAdapter: TagListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTagListingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        defineToolbar()

        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.setOnRefreshListener {
            refreshList()
        }

        recyclerViewEvent = binding.recyclerViewEventReceipts
        tagListAdapter = TagListAdapter(
            requireContext(), listingViewModel.tagList.value ?: arrayListOf(), this
        ) { index ->
            listingViewModel.tagList.value?.get(index)?.let { category ->
                popUpButtonSheet(index, category)
            }
        }
        recyclerViewEvent.adapter = tagListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.tagSearchInput.doOnTextChanged { text, _, _, _ ->
            listingViewModel.filterCategoryList.value?.category = text.toString()
            listingViewModel.loadDataByCategoryFilter()
        }
        binding.tagSearchLayout.setStartIconOnClickListener {
            binding.tagSearchInput.setText("")
            listingViewModel.filterCategoryList.value?.category = ""
        }
        binding.newButton.setOnClickListener {
            val action =
                ListingFragmentDirections.actionListingFragmentToAddTagFragment(
                    inputType = AddingInputType.EMPTY.name,
                    tagId = "",
                    sourceFragment = FragmentName.TAG_LIST_FRAGMENT
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    private fun defineToolbar() {
        binding.toolbar.inflateMenu(R.menu.top_menu_list_filter)
        binding.toolbar.menu.findItem(R.id.filter).isVisible = false
        binding.toolbar.menu.findItem(R.id.collapse).isVisible = false
        binding.toolbar.menu.findItem(R.id.expand).isVisible = false
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refresh -> {
                    refreshList()
                    true
                }

                else -> false
            }
        }
    }

    private fun refreshList() {
        listingViewModel.loadDataByTagFilter()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        listingViewModel.tagList.observe(viewLifecycleOwner) {
            it?.let {
                tagListAdapter.tagList = it
                tagListAdapter.notifyDataSetChanged()
                binding.refreshLayout.isRefreshing = false
            }
        }
        listingViewModel.filterCategoryList.observe(viewLifecycleOwner) {
            putFilterDefinitionIntoInputs()
        }
    }

    private fun onDelete(index: Int, tag: Tag) {
        ConfirmDialog(
            getString(R.string.delete_confirmation_title),
            getString(R.string.delete_category_confirmation_dialog)
        ) {
            if (listingViewModel.productTagList.value?.none { productTag ->
                    productTag.id == tag.id
                } != true) {
                Toast.makeText(
                    requireContext(),
                    "Cannot delete because of existing products",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                addTagDataViewModel.deleteTag(tag.id) {
                    listingViewModel.reloadOutdatedTagList.postValue(true)
                    listingViewModel.reloadOutdatedProductTagList.postValue(true)
                    listingViewModel.tagList.value?.let { tagList ->
                        tagList.removeAt(index)
                        tagListAdapter.tagList = tagList
                        tagListAdapter.notifyItemRemoved(index)
                        tagListAdapter.notifyItemRangeChanged(
                            index, tagListAdapter.tagList.size
                        )
                    }
                    listingViewModel.loadDataByProductFilter()
                }

            }
        }.show(childFragmentManager, "TAG")
    }

    private fun popUpButtonSheet(index: Int, tag: Tag) {
        val modalBottomSheet = TagBottomSheetFragment { onDelete(index, tag) }
        modalBottomSheet.show(parentFragmentManager, TagBottomSheetFragment.TAG)
    }

    private fun putFilterDefinitionIntoInputs() {
        listingViewModel.filterTagList.value?.let {
            if (binding.tagSearchInput.text.toString() != it.tagName) {
                binding.tagSearchInput.setText(it.tagName)
            }
        }
    }

    override fun onItemClick(index: Int) {
        val tag = listingViewModel.tagList.value!![index]
        val action = ListingFragmentDirections.actionListingFragmentToAddTagFragment(
            tagId = tag.id,
            inputType = AddingInputType.ID.name,
            sourceFragment = FragmentName.TAG_LIST_FRAGMENT
        )
        Navigation.findNavController(requireView()).navigate(action)
    }
}
