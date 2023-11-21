package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.DiffUtilCallback
import com.example.navsample.databinding.RowExperimentBinding

class ExperimentalListAdapter(
    var context: Context,
    var recycleList: ArrayList<ExperimentalAdapterArgument>,

    var onClick: (Int) -> Unit,
    var onLongClick: (Int) -> Unit,
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
        holder.binding.mainLayout.setOnLongClickListener {
            onLongClick.invoke(holder.adapterPosition)
            true
        }

    }

    override fun getItemCount(): Int {
        return recycleList.size
    }

    fun setData(data: ArrayList<ExperimentalAdapterArgument>) {
        recycleList = data
    }

    // add new data
    fun setNewData(newData: MutableList<ExperimentalAdapterArgument>) {
        val diffCallback = DiffUtilCallback(recycleList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recycleList.clear()
        recycleList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}
