package com.example.navsample.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.navsample.fragments.listing.CategoryListFragment
import com.example.navsample.fragments.listing.ProductListFragment
import com.example.navsample.fragments.listing.ReceiptListFragment
import com.example.navsample.fragments.listing.StoreListFragment

class ViewPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StoreListFragment()
            1 -> ReceiptListFragment()
            2 -> ProductListFragment()
            3 -> CategoryListFragment()
            else -> StoreListFragment()

        }
    }
}