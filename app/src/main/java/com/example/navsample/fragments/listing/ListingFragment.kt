package com.example.navsample.fragments.listing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.navsample.ApplicationContext
import com.example.navsample.activity.GuideActivity
import com.example.navsample.adapters.ViewPagerAdapter
import com.example.navsample.databinding.FragmentListingBinding
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.firestore.TranslateFirebaseEntity
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.ListingViewModel
import com.example.navsample.viewmodels.SyncDatabaseViewModel
import com.google.android.material.tabs.TabLayout


class ListingFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val syncDatabaseViewModel: SyncDatabaseViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by activityViewModels()

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

        imageViewModel.clearData()
        binding.viewPager.adapter = viewPagerAdapter
        if (shouldRunGuide()) {
            markRunGuideAsDone()
            val intent = Intent(requireContext(), GuideActivity::class.java)
            startActivity(intent)
        }

        initObserver()
        syncDatabaseViewModel.loadAllList()
        syncDatabaseViewModel.loadNotAddedList()
        syncDatabaseViewModel.readFirestoreChanges()


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

    private fun shouldRunGuide(): Boolean {
        return ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
            ?.getBoolean("shouldRunGuide", true)
            ?: true
    }


    private fun markRunGuideAsDone() {
        val myPref = ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        myPref?.edit()?.putBoolean("shouldRunGuide", false)?.apply()
    }

    private fun initNotSyncedLists() {
        syncDatabaseViewModel.notSyncedCategoryList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { category ->
                    syncDatabaseViewModel.syncStatusOperation(category) {
                        listingViewModel.loadDataByCategoryFilter()
                    }
                },
                { syncDatabaseViewModel.loadNotSynced(Category::class) },
                "notSyncedCategoryList"
            )
        }
        syncDatabaseViewModel.notSyncedTagList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { tag ->
                    syncDatabaseViewModel.syncStatusOperation(tag)
                    { listingViewModel.loadDataByTagFilter() }
                },
                { syncDatabaseViewModel.loadNotSynced(Tag::class) },
                "notSyncedTagList"
            )
        }
        syncDatabaseViewModel.notSyncedProductTagList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { productTag ->
                    syncDatabaseViewModel.syncStatusOperation(productTag)
                    { listingViewModel.refreshProductTagList() }
                },
                { syncDatabaseViewModel.loadNotSynced(ProductTagCrossRef::class) },
                "notSyncedProductTagList"
            )
        }
        syncDatabaseViewModel.notSyncedStoreList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { store ->
                    syncDatabaseViewModel.syncStatusOperation(store)
                    { listingViewModel.loadDataByStoreFilter() }
                },
                { syncDatabaseViewModel.loadNotSynced(Store::class) },
                "notSyncedStoreList"
            )
        }
        syncDatabaseViewModel.notSyncedReceiptList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { receipt ->
                    syncDatabaseViewModel.syncStatusOperation(receipt)
                    { listingViewModel.loadDataByReceiptFilter() }
                },
                { syncDatabaseViewModel.loadNotSynced(Receipt::class) },
                "notSyncedReceiptList"
            )
        }
        syncDatabaseViewModel.notSyncedProductList.observe(viewLifecycleOwner) {
            observerForNotSynced(
                it,
                { product ->
                    syncDatabaseViewModel.syncStatusOperation(product)
                    { listingViewModel.loadDataByProductFilter() }
                },
                { syncDatabaseViewModel.loadNotSynced(Product::class) },
                "notSyncedProductList"
            )
        }
    }

    private fun initOutdatedLists() {
        syncDatabaseViewModel.outdatedCategoryList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedCategoryList size: ${it.size}")
            it?.forEach { category ->
                syncDatabaseViewModel.syncOutdatedCategory(category)
            }
        }
        syncDatabaseViewModel.outdatedTagList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedTagList size: ${it.size}")
            it?.forEach { tag ->
                syncDatabaseViewModel.syncOutdatedTag(tag)
            }
        }
        syncDatabaseViewModel.outdatedProductTagList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedProductTagList size: ${it.size}")
            it?.forEach { productTag ->
                syncDatabaseViewModel.syncOutdatedProductTag(productTag)
            }
        }
        syncDatabaseViewModel.outdatedStoreList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedStoreList size: ${it.size}")
            it?.forEach { store ->
                syncDatabaseViewModel.syncOutdatedStore(store)
            }
        }
        syncDatabaseViewModel.outdatedReceiptList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedReceiptList size: ${it.size}")
            it?.forEach { receipt ->
                syncDatabaseViewModel.syncOutdatedReceipt(receipt)
            }
        }
        syncDatabaseViewModel.outdatedProductList.observe(viewLifecycleOwner) {
            Log.i("Firebase", "outdatedProductList size: ${it.size}")
            it?.forEach { product ->
                syncDatabaseViewModel.syncOutdatedProduct(product)
            }
        }
    }

    private fun initObserver() {
        if (!syncDatabaseViewModel.isFirebaseActive()) {
            return
        }
        initOutdatedLists()
        initNotSyncedLists()
        readEntitiesList()
        reloadOutdatedLists()

    }

    private fun reloadOutdatedLists() {
        listingViewModel.reloadOutdatedCategoryList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(Category::class)
            }
        }
        listingViewModel.reloadOutdatedStoreList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(Store::class)
            }
        }
        listingViewModel.reloadOutdatedReceiptList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(Receipt::class)
            }
        }
        listingViewModel.reloadOutdatedProductRichList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(Product::class)
            }
        }
        listingViewModel.reloadOutdatedTagList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(Tag::class)
            }
        }
        listingViewModel.reloadOutdatedProductTagList.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.loadOutdated(ProductTagCrossRef::class)
            }
        }
    }

    private fun readEntitiesList() {
        syncDatabaseViewModel.categoryRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.categoryRead.postValue(false)
                listingViewModel.loadDataByCategoryFilter()
                listingViewModel.loadDataByStoreFilter()
                listingViewModel.loadDataByProductFilter()
            }
        }
        syncDatabaseViewModel.storeRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.storeRead.postValue(false)
                listingViewModel.loadDataByStoreFilter()
                listingViewModel.loadDataByReceiptFilter()
                listingViewModel.loadDataByProductFilter()
                listingViewModel.refreshProductTagList()
            }
        }
        syncDatabaseViewModel.receiptRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.receiptRead.postValue(false)
                listingViewModel.loadDataByReceiptFilter()
            }
        }
        syncDatabaseViewModel.productRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.productRead.postValue(false)
                listingViewModel.loadDataByProductFilter()
                listingViewModel.refreshProductTagList()
                listingViewModel.loadDataByReceiptFilter()
            }
        }
        syncDatabaseViewModel.tagRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.tagRead.postValue(false)
                listingViewModel.loadDataByTagFilter()
                listingViewModel.loadDataByProductFilter()
                listingViewModel.refreshProductTagList()
            }
        }
        syncDatabaseViewModel.productTagRead.observe(viewLifecycleOwner) {
            if (it) {
                syncDatabaseViewModel.productTagRead.postValue(false)
                listingViewModel.loadDataByProductFilter()
                listingViewModel.loadDataByTagFilter()
                listingViewModel.refreshProductTagList()
            }
        }
    }

    private fun <T : TranslateFirebaseEntity> observerForNotSynced(
        notSyncedList: List<T>,
        syncStatus: (T) -> Boolean,
        loadNotSynced: () -> Unit,
        logName: String
    ) {
        Log.i("Firebase", "$logName size: ${notSyncedList.size}")
        var operationPerformed = true
        notSyncedList.forEach { entity ->
            if (!syncStatus(entity)) {
                operationPerformed = false

            }
        }
        if (!operationPerformed) {
            loadNotSynced()
        }
    }
}
