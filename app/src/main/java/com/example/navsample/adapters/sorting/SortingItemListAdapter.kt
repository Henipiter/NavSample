package com.example.navsample.adapters.sorting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.databinding.RowExperimentBinding
import com.example.navsample.dto.sorting.ItemAdapterArgument

abstract class SortingItemListAdapter<E : ItemAdapterArgument>(
    var recycleList: ArrayList<E>
) : RecyclerView.Adapter<SortingItemListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recycleList.size
    }
}
