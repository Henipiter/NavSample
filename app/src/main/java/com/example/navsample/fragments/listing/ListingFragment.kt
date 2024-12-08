package com.example.navsample.fragments.listing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.navsample.adapters.ViewPagerAdapter
import com.example.navsample.databinding.FragmentListingBinding
import com.example.navsample.entities.dto.TranslateFirebaseEntity
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
        syncDatabaseViewModel.setFirebaseHelper()
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
        if (!syncDatabaseViewModel.isFirebaseActive()) {
            return
        }
        syncDatabaseViewModel.outdatedCategoryList.observe(viewLifecycleOwner) {
            Log.i(
                "Firebase",
                "outdatedCategoryList size: ${syncDatabaseViewModel.outdatedCategoryList.value?.size ?: "empty"}"
            )
            it?.forEach { category ->
                syncDatabaseViewModel.syncOutdatedCategory(category)
            }
        }
        syncDatabaseViewModel.outdatedStoreList.observe(viewLifecycleOwner) {
            Log.i(
                "Firebase",
                "outdatedStoreList size: ${syncDatabaseViewModel.outdatedStoreList.value?.size ?: "empty"}"
            )
            it?.forEach { store ->
                syncDatabaseViewModel.syncOutdatedStore(store)
            }
        }
        syncDatabaseViewModel.outdatedReceiptList.observe(viewLifecycleOwner) {
            Log.i(
                "Firebase",
                "outdatedReceiptList size: ${syncDatabaseViewModel.outdatedReceiptList.value?.size ?: "empty"}"
            )
            it?.forEach { receipt ->
                syncDatabaseViewModel.syncOutdatedReceipt(receipt)
            }
        }
        syncDatabaseViewModel.outdatedProductList.observe(viewLifecycleOwner) {
            Log.i(
                "Firebase",
                "outdatedProductList size: ${syncDatabaseViewModel.outdatedProductList.value?.size ?: "empty"}"
            )
            it?.forEach { product ->
                syncDatabaseViewModel.syncOutdatedProduct(product)
            }
        }

        syncDatabaseViewModel.notSyncedCategoryList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { category -> syncDatabaseViewModel.categorySyncStatusOperation(category) },
                { syncDatabaseViewModel.loadNotSyncedCategories() },
                "notSyncedCategoryList"
            )
        }
        syncDatabaseViewModel.notSyncedStoreList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { store -> syncDatabaseViewModel.storeSyncStatusOperation(store) },
                { syncDatabaseViewModel.loadNotSyncedStores() },
                "notSyncedStoreList"
            )
        }
        syncDatabaseViewModel.notSyncedReceiptList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { receipt -> syncDatabaseViewModel.receiptSyncStatusOperation(receipt) },
                { syncDatabaseViewModel.loadNotSyncedReceipts() },
                "notSyncedReceiptList"
            )
        }
        syncDatabaseViewModel.notSyncedProductList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { product -> syncDatabaseViewModel.productSyncStatusOperation(product) },
                { syncDatabaseViewModel.loadNotSyncedProducts() },
                "notSyncedProductList"
            )
        }
    }

    private fun <T : TranslateFirebaseEntity> observerForNotSynced(
        notSyncedList: List<T>?,
        syncStatus: (T) -> Boolean,
        loadNotSynced: () -> Unit,
        logName: String
    ) {
        Log.i(
            "Firebase",
            "$logName size: ${notSyncedList?.size ?: "empty"}"
        )
        var operationPerformed = true
        notSyncedList?.forEach { product ->
            if (!syncStatus(product)) {
                operationPerformed = false

            }
        }
        if (!operationPerformed) {
            loadNotSynced()
        }
    }
}
