package com.example.navsample.sorting

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class NonScrollableGridLayoutManager(context: Context?, spanCount: Int) :
    GridLayoutManager(context, spanCount) {
    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }
}