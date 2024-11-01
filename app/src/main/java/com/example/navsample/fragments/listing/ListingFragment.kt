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
        syncDatabaseViewModel.categoryList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { category ->
                if (!syncDatabaseViewModel.categoryOperation(category)) {
                    operationPerformed = false
                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadCategories()
            }
        }
        syncDatabaseViewModel.storeList.observe(viewLifecycleOwner) {
            var operationPerformed = true
            it?.forEach { store ->
                if (!syncDatabaseViewModel.storeOperation(store)) {
                    operationPerformed = false

                }
            }
            if (!operationPerformed) {
                syncDatabaseViewModel.loadStores()
            }
        }
//        syncDatabaseViewModel.receiptList.observe(viewLifecycleOwner) {
//            it?.forEach { receipt -> syncDatabaseViewModel.syncReceipt(receipt) }
//        }
//        syncDatabaseViewModel.productList.observe(viewLifecycleOwner) {
//            it?.forEach { product -> syncDatabaseViewModel.syncProduct(product) }
//        }
    }
}