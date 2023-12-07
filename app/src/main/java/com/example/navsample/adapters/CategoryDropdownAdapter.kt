package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.navsample.databinding.ArrayAdapterRowBinding
import com.example.navsample.entities.Category

class CategoryDropdownAdapter(
    ctx: Context, res: Int, private var categoryList: ArrayList<Category>
) : ArrayAdapter<Category>(ctx, res, categoryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = convertView?.tag as? ArrayAdapterRowBinding ?: ArrayAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.tag = binding
        binding.name.text = categoryList[position].name
        return binding.root
    }
}