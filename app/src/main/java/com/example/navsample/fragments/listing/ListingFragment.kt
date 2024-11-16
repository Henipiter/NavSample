package com.example.navsample.fragments.listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.navsample.adapters.ViewPagerAdapter
import com.example.navsample.databinding.FragmentListingBinding
import com.example.navsample.viewmodels.SyncDatabaseViewModel
import com.google.android.material.tabs.TabLayout


class ListingFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val syncDatabaseViewModel: SyncDatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter
        initObserver()
        syncDatabaseViewModel.loadAllList()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.viewPager.currentItem = tab.position
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.getTabAt(position)?.select()
            }
        })
    }

    private fun initObserver() {
        syncDatabaseViewModel.outdatedCategoryList.observe(viewLifecycleOwner) {
            it?.forEach { category ->
                syncDatabaseViewModel.syncOutdatedCategory(category)
            }
        }
        syncDatabaseViewModel.outdatedStoreList.observe(viewLifecycleOwner) {
            it?.forEach { store ->
                syncDatabaseViewModel.syncOutdatedStore(store)
            }
        }
        syncDatabaseViewModel.outdatedReceiptList.observe(viewLifecycleOwner) {
            it?.forEach { receipt ->
                syncDatabaseViewModel.syncOutdatedReceipt(receipt)
            }
        }
        syncDatabaseViewModel.outdatedProductList.observe(viewLifecycleOwner) {
            it?.forEach { product ->
                syncDatabaseViewModel.syncOutdatedProduct(product)
            }
        }

        syncDatabaseViewModel.notSyncedCategoryList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { category ->
                if (!syncDatabaseViewModel.categorySyncStatusOperation(category)) {
                    operationPerformed = false
                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadNotSyncedCategories()
            }
        }
        syncDatabaseViewModel.notSyncedStoreList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { store ->
                if (!syncDatabaseViewModel.storeSyncStatusOperation(store)) {
                    operationPerformed = false

                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadNotSyncedStores()
            }
        }
        syncDatabaseViewModel.notSyncedReceiptList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { receipt ->
                if (syncDatabaseViewModel.receiptSyncStatusOperation(receipt)) {
                    operationPerformed = false

                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadNotSyncedReceipts()
            }
        }
        syncDatabaseViewModel.notSyncedProductList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { product ->
                if (syncDatabaseViewModel.productSyncStatusOperation(product)) {
                    operationPerformed = false

                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadNotSyncedProducts()
            }
        }
    }
}
