package com.example.navsample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.UserDiffUtilCallback
import com.example.navsample.databinding.RowExperimentBinding
import com.example.navsample.dto.SortingElementColor
import com.example.navsample.dto.Type
import com.example.navsample.dto.UserItemAdapterArgument

class UserItemListAdapter(
    var recycleList: ArrayList<UserItemAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
) : RecyclerView.Adapter<UserItemListAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: RowExperimentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value

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

    fun setData(data: ArrayList<UserItemAdapterArgument>) {
        recycleList = data
    }

    // add new data
    fun setNewData(newData: MutableList<UserItemAdapterArgument>) {
        val diffCallback = UserDiffUtilCallback(recycleList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recycleList.clear()
        recycleList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}