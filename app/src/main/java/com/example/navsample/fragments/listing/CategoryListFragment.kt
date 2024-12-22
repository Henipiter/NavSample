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
import com.example.navsample.adapters.CategoryListAdapter
import com.example.navsample.databinding.FragmentCategoryListBinding
import com.example.navsample.dto.FragmentName
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.fragments.dialogs.ConfirmDialog
import com.example.navsample.sheets.CategoryBottomSheetFragment
import com.example.navsample.sheets.ReceiptBottomSheetFragment
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.fragment.AddCategoryDataViewModel


class CategoryListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!
    private val listingViewModel: ListingViewModel by activityViewModels()
    private val addCategoryDataViewModel: AddCategoryDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var categoryListAdapter: CategoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        listingViewModel.refreshProductList()

        recyclerViewEvent = binding.recyclerViewEventReceipts
        categoryListAdapter = CategoryListAdapter(
            requireContext(), listingViewModel.categoryList.value ?: arrayListOf(), this
        ) { index ->
            listingViewModel.categoryList.value?.get(index)?.let { category ->
                popUpButtonSheet(index, category)
            }
        }
        recyclerViewEvent.adapter = categoryListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.categoryNameInput.doOnTextChanged { text, _, _, _ ->
            listingViewModel.filterCategoryList.value?.category = text.toString()
            listingViewModel.loadDataByCategoryFilter()
        }
        binding.categoryNameLayout.setStartIconOnClickListener {
            binding.categoryNameInput.setText("")
            listingViewModel.filterCategoryList.value?.category = ""
        }
        binding.newButton.setOnClickListener {
            val action =
                ListingFragmentDirections.actionListingFragmentToAddCategoryFragment(
                    inputType = AddingInputType.EMPTY.name,
                    categoryId = "",
                    sourceFragment = FragmentName.CATEGORY_LIST_FRAGMENT
                )
            Navigation.findNavController(requireView()).navigate(action)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        listingViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                categoryListAdapter.categoryList = it
                categoryListAdapter.notifyDataSetChanged()
            }
        }
        listingViewModel.filterCategoryList.observe(viewLifecycleOwner) {
            putFilterDefinitionIntoInputs()
        }
    }

    private fun onDelete(index: Int, category: Category) {
        ConfirmDialog(
            getString(R.string.delete_confirmation_title),
            getString(R.string.delete_category_confirmation_dialog)
        ) {
            if (listingViewModel.productRichList.value?.none { product -> product.categoryId == category.id } != true) {
                Toast.makeText(
                    requireContext(),
                    "Cannot delete because of existing products",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                addCategoryDataViewModel.deleteCategory(category.id)
                listingViewModel.categoryList.value?.let { categoryList ->
                    categoryList.removeAt(index)
                    categoryListAdapter.categoryList = categoryList
                    categoryListAdapter.notifyItemRemoved(index)
                    categoryListAdapter.notifyItemRangeChanged(
                        index, categoryListAdapter.categoryList.size
                    )
                }

            }
        }.show(childFragmentManager, "TAG")
    }

    private fun popUpButtonSheet(index: Int, category: Category) {
        val modalBottomSheet = CategoryBottomSheetFragment { onDelete(index, category) }
        modalBottomSheet.show(parentFragmentManager, ReceiptBottomSheetFragment.TAG)
    }

    private fun putFilterDefinitionIntoInputs() {
        listingViewModel.filterCategoryList.value?.let {
            if (binding.categoryNameInput.text.toString() != it.category) {
                binding.categoryNameInput.setText(it.category)
            }
        }
    }

    override fun onItemClick(index: Int) {
        val category = listingViewModel.categoryList.value!![index]
        val action = ListingFragmentDirections.actionListingFragmentToAddCategoryFragment(
            categoryId = category.id,
            inputType = AddingInputType.ID.name,
            sourceFragment = FragmentName.CATEGORY_LIST_FRAGMENT
        )
        Navigation.findNavController(requireView()).navigate(action)
    }
}