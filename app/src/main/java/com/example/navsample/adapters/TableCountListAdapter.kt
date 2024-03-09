package com.example.navsample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.databinding.TableCountRowBinding
import com.example.navsample.entities.relations.TableCounts

class TableCountListAdapter(var recycleList: ArrayList<TableCounts>) :
    RecyclerView.Adapter<TableCountListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: TableCountRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            TableCountRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tableName.text = recycleList[holder.adapterPosition].tableName
        holder.binding.tableCount.text = recycleList[holder.adapterPosition].count.toString()
    }

    override fun getItemCount(): Int {
        return recycleList.size
    }
}
