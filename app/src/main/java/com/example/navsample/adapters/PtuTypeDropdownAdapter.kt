package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.example.navsample.databinding.ArrayAdapterRowBinding

class PtuTypeDropdownAdapter(
    ctx: Context,
    res: Int
) : ArrayAdapter<String>(ctx, res), Filterable {
    var filteredList = arrayListOf("A", "B", "C", "D", "E", "F", "G")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = convertView?.tag as? ArrayAdapterRowBinding ?: ArrayAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.tag = binding
        binding.name.text = filteredList[position]
        return binding.root
    }

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): String {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {


                val results = FilterResults()
                results.values = filteredList
                results.count = filteredList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val values = results?.values
                filteredList = if (values is ArrayList<*>) {
                    values.map { it as String } as ArrayList
                } else {
                    arrayListOf()
                }
                notifyDataSetChanged()
            }
        }

    }
}