package com.example.navsample.adapters

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.navsample.entities.Store

class StoreDropdownAdapter(
    var ctx: Context, var res: Int, var storeList: ArrayList<Store>
) : ArrayAdapter<Store>(ctx, res, storeList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val binding: ArrayAdapterRowBinding
//        if (convertView == null) {
//            binding = ArrayAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//            binding.root.tag = binding
//        } else {
//            binding = convertView.tag as ArrayAdapterRowBinding
//        }
//        val store = getItem(position)
//        binding.text1.text = store?.name ?: ""
//
//        return binding.root

        val store = storeList[position]

        class ViewHolder(val textView: TextView)

        val viewHolder: ViewHolder
        val rowView: View

        if (convertView == null) {
            rowView =
                LayoutInflater.from(context)
                    .inflate(com.example.navsample.R.layout.array_adapter_row, parent, false)
            viewHolder = ViewHolder(rowView.findViewById(R.id.text1))

        } else {
            rowView = convertView
            viewHolder = (convertView.tag as? ViewHolder) ?: ViewHolder(
                rowView.findViewById(R.id.text1)
            )
        }

        rowView.tag = viewHolder
        viewHolder.textView.text = store.name

        return rowView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {


        val rowView = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.simple_spinner_item, parent, false
        )

        val textView: TextView = rowView.findViewById(android.R.id.text1)
        val mojObiekt = getItem(position)
        textView.text = mojObiekt?.name ?: ""

        return rowView
    }

}