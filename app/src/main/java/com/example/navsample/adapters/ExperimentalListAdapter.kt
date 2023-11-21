package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.databinding.RowExperimentBinding

class ExperimentalListAdapter(
    var context: Context,
    var recycleList: ArrayList<ExperimentalAdapterArgument>,

    var onClick: (Int) -> Unit
) : RecyclerView.Adapter<ExperimentalListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value
        holder.binding.mainLayout.setBackgroundColor(recycleList[holder.adapterPosition].color)
        holder.binding.mainLayout.setOnClickListener {
            onClick.invoke(holder.adapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return recycleList.size
    }
}
