package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.example.navsample.databinding.ArrayAdapterRowBinding
import com.example.navsample.entities.Category

class CategoryDropdownAdapter(
    ctx: Context,
    res: Int,
    var categoryList: ArrayList<Category>
) : ArrayAdapter<Category>(ctx, res, categoryList), Filterable {
    private var filteredList = ArrayList<Category>(categoryList)
    private val addNewCategoryHolder = Category("+ ADD NEW", "")

    init {
        filteredList = ArrayList(categoryList)
        filteredList.add(addNewCategoryHolder)
    }

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

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): Category {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val filteredList: List<Category>
                val filterText = constraint?.toString()?.uppercase()
                if (filterText.isNullOrBlank()) {
                    filteredList = categoryList
                    filteredList.sortedBy { it.name }
                } else {
                    filteredList = categoryList.filter { it.name.uppercase().contains(filterText) }
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
                val values = results?.values
                if (values is ArrayList<*>) {
                    filteredList = values.map { it as Category } as ArrayList
                    if (filteredList.size == 0 || filteredList.last().color != addNewCategoryHolder.color) {
                        filteredList.add(addNewCategoryHolder)
                    }
                } else {
                    filteredList = arrayListOf()
                }
                notifyDataSetChanged()
            }
        }

    }
}
