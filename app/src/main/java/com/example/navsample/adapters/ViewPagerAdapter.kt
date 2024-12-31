package com.example.navsample.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.navsample.fragments.listing.CategoryListFragment
import com.example.navsample.fragments.listing.ProductListFragment
import com.example.navsample.fragments.listing.ReceiptListFragment
import com.example.navsample.fragments.listing.StoreListFragment
import com.example.navsample.fragments.listing.TagListingFragment

class ViewPagerAdapter(var fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReceiptListFragment()
            1 -> ProductListFragment()
            2 -> StoreListFragment()
            3 -> CategoryListFragment()
            4 -> TagListingFragment()
            else -> ReceiptListFragment()
        }
    }
}