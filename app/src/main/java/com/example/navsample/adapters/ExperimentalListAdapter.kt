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
import com.example.navsample.dto.Type

class ExperimentalListAdapter(
    var recycleList: ArrayList<ExperimentalAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
) : RecyclerView.Adapter<ExperimentalListAdapter.MyViewHolder>() {
    companion object {
        val INACTIVE = Color.rgb(140, 140, 140)
        val ACTIVE = Color.rgb(160, 125, 215)

        val PRICE = Color.rgb(0, 0, 255)
        val PRODUCT = Color.rgb(0, 255, 0)
        val UNCHECKED = Color.rgb(200, 200, 200)
    }

    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value
        holder.binding.counter.text = recycleList[holder.adapterPosition].number.toString()

        when (recycleList[holder.adapterPosition].chosen) {
            true -> {
                holder.binding.mainLayout.setBackgroundColor(ACTIVE)
                holder.binding.counter.visibility = View.VISIBLE
            }

            false -> {
                holder.binding.mainLayout.setBackgroundColor(INACTIVE)
                holder.binding.counter.visibility = View.INVISIBLE
            }
        }

        when (recycleList[holder.adapterPosition].type) {
            Type.NAME -> holder.binding.typeColor.setBackgroundColor(PRODUCT)
            Type.PRICE -> holder.binding.typeColor.setBackgroundColor(PRICE)
            else -> holder.binding.typeColor.setBackgroundColor(UNCHECKED)
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
