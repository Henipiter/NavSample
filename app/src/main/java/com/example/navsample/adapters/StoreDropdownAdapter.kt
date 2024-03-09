package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.example.navsample.databinding.ArrayAdapterRowBinding
import com.example.navsample.entities.Store

class StoreDropdownAdapter(
    ctx: Context,
    res: Int,
    var storeList: ArrayList<Store>
) : ArrayAdapter<Store>(ctx, res, storeList), Filterable {

    private var filteredList = ArrayList<Store>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = convertView?.tag as? ArrayAdapterRowBinding ?: ArrayAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.tag = binding
        binding.name.text = filteredList[position].name
        binding.nip.text = filteredList[position].nip
        return binding.root
    }

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): Store {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val filteredList: List<Store>
                val filterText = constraint?.toString()?.uppercase()
                if (filterText.isNullOrBlank()) {
                    filteredList = storeList
                    filteredList.sortedBy { it.name }
                } else {
                    filteredList = storeList.filter { it.name.uppercase().contains(filterText) }
                    filteredList.sortedWith(compareBy({
                        it.name.indexOf(
                            filterText,
                            ignoreCase = true
                        )
                    }, { it.name }))
                }
                val results = FilterResults()
                results.values = filteredList
                results.count = filteredList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? ArrayList<Store> ?: arrayListOf()
                notifyDataSetChanged()
            }
        }
    }

}
