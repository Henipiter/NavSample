package com.example.navsample.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DiffUtilCallback
import com.example.navsample.databinding.RowExperimentBinding
import com.example.navsample.dto.ExperimentalAdapterArgument

class ExperimentalListAdapter(
    var recycleList: ArrayList<ExperimentalAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
) : RecyclerView.Adapter<ExperimentalListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value
        holder.binding.counter.text = recycleList[holder.adapterPosition].number.toString()
        if (recycleList[holder.adapterPosition].chosen) {
            holder.binding.mainLayout.setBackgroundColor(Color.YELLOW)
            holder.binding.counter.visibility = View.VISIBLE
        } else {
            holder.binding.mainLayout.setBackgroundColor(Color.GRAY)
            holder.binding.counter.visibility = View.INVISIBLE
        }
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
