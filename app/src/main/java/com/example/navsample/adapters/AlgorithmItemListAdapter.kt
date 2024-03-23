package com.example.navsample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.databinding.RowExperimentBinding
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.SortingElementColor
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.sorting.AlgorithmDiffUtilCallback

class AlgorithmItemListAdapter(
    var recycleList: ArrayList<AlgorithmItemAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
) : RecyclerView.Adapter<AlgorithmItemListAdapter.MyViewHolder>() {


    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value
        holder.binding.counter.text = recycleList[holder.adapterPosition].number.toString()

        when (recycleList[holder.adapterPosition].status) {
            Status.CHOSEN -> {
                holder.binding.mainLayout.setBackgroundColor(SortingElementColor.CHOSEN)
                holder.binding.counter.visibility = View.VISIBLE
            }

            Status.DEFAULT -> {
                holder.binding.mainLayout.setBackgroundColor(SortingElementColor.DEFAULT)
                holder.binding.counter.visibility = View.INVISIBLE
            }

            Status.BLOCKED -> {
                holder.binding.mainLayout.setBackgroundColor(SortingElementColor.BLOCKED)
                holder.binding.counter.visibility = View.INVISIBLE
            }
        }

        when (recycleList[holder.adapterPosition].type) {
            Type.NAME -> holder.binding.typeColor.setBackgroundColor(SortingElementColor.NAME)
            Type.PRICE -> holder.binding.typeColor.setBackgroundColor(SortingElementColor.PRICE)
            else -> holder.binding.typeColor.setBackgroundColor(SortingElementColor.UNCHECKED)
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

    fun setData(data: ArrayList<AlgorithmItemAdapterArgument>) {
        recycleList = data
    }

    // add new data
    fun setNewData(newData: MutableList<AlgorithmItemAdapterArgument>) {
        val diffCallback = AlgorithmDiffUtilCallback(recycleList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recycleList.clear()
        recycleList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}
