package com.example.navsample.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.navsample.fragments.listing.CategoryListFragment
import com.example.navsample.fragments.listing.ProductListFragment
import com.example.navsample.fragments.listing.ReceiptListFragment
import com.example.navsample.fragments.listing.StoreListFragment

class ViewPagerAdapter(var fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReceiptListFragment()
            1 -> ProductListFragment()
            2 -> StoreListFragment()
            3 -> CategoryListFragment()
            else -> ReceiptListFragment()
        }
    }
}