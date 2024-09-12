package com.example.navsample.fragments.listing

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
import com.example.navsample.adapters.CategoryListAdapter
import com.example.navsample.databinding.FragmentCategoryListBinding
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel


class CategoryListFragment : Fragment(), ItemClickListener {
    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

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
        refreshList()
        receiptDataViewModel.refreshProductList()

        recyclerViewEvent = binding.recyclerViewEventReceipts
        categoryListAdapter = CategoryListAdapter(
            requireContext(), receiptDataViewModel.categoryList.value ?: arrayListOf(), this
        ) { i ->
            receiptDataViewModel.categoryList.value?.get(i)?.let {
                DeleteConfirmationDialog("Are you sure you want to delete the category products??\n\nName: " + it.name) {
                    if (receiptDataViewModel.productRichList.value?.none { product -> product.categoryId == it.id } != true) {
                        Toast.makeText(
                            requireContext(),
                            "Cannot delete beacuse of existing products",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        receiptDataViewModel.deleteCategory(it)
                        receiptDataViewModel.categoryList.value?.removeAt(i)
                        categoryListAdapter.categoryList =
                            receiptDataViewModel.categoryList.value ?: arrayListOf()
                        categoryListAdapter.notifyItemRemoved(i)
                    }
                }.show(childFragmentManager, "TAG")
            }
        }
        recyclerViewEvent.adapter = categoryListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.categoryNameInput.doOnTextChanged { text, _, _, _ ->
            receiptDataViewModel.filterCategoryList.value?.category = text.toString()
            refreshList()
        }
        binding.newButton.setOnClickListener {
            val action = ListingFragmentDirections.actionListingFragmentToAddCategoryFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }

    }

    private fun initObserver() {
        receiptDataViewModel.categoryList.observe(viewLifecycleOwner) {
            it?.let {
                categoryListAdapter.categoryList = it
                categoryListAdapter.notifyDataSetChanged()
            }
        }
        receiptDataViewModel.filterCategoryList.observe(viewLifecycleOwner) {
            refreshList()
        }
    }

    private fun putFilterDefinitionIntoInputs() {
        receiptDataViewModel.filterCategoryList.value?.let {
            if (binding.categoryNameInput.text.toString() != it.category) {
                binding.categoryNameInput.setText(it.category)
            }
        }
    }

    private fun refreshList() {
        receiptDataViewModel.filterCategoryList.value?.let {
            putFilterDefinitionIntoInputs()
            receiptDataViewModel.refreshCategoryList(it.category)
        }
    }

    override fun onItemClick(index: Int) {
        val category = receiptDataViewModel.categoryList.value!![index]
        receiptDataViewModel.savedCategory.value = category
        receiptDataViewModel.category.value = category
        val action = ListingFragmentDirections.actionListingFragmentToAddCategoryFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }
}