package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.navsample.databinding.ArrayAdapterRowBinding
import com.example.navsample.entities.Store

class StoreDropdownAdapter(
    ctx: Context, var res: Int, private var storeList: ArrayList<Store>
) : ArrayAdapter<Store>(ctx, res, storeList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = convertView?.tag as? ArrayAdapterRowBinding ?: ArrayAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.tag = binding

        binding.text1.text = storeList[position].name

        return binding.root
    }
}