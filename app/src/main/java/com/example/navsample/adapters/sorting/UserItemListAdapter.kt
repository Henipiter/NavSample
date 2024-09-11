package com.example.navsample.adapters.sorting

import androidx.recyclerview.widget.DiffUtil
import com.example.navsample.dto.SortingElementColor
import com.example.navsample.dto.Type
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.sorting.UserDiffUtilCallback

class UserItemListAdapter(
    recycleList: ArrayList<UserItemAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
) : SortingItemListAdapter<UserItemAdapterArgument>(recycleList) {


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value

        when (recycleList[holder.adapterPosition].type) {
            Type.NAME -> {
                holder.binding.typeColor.setBackgroundColor(SortingElementColor.NAME)
            }

            Type.PRICE -> {
                holder.binding.typeColor.setBackgroundColor(SortingElementColor.PRICE)
            }

            else -> holder.binding.typeColor.setBackgroundColor(SortingElementColor.UNCHECKED)
        }
        holder.binding.counter.text = recycleList[position].algorithmItem.number.toString()

        holder.binding.mainLayout.setOnClickListener {
            onClick.invoke(holder.adapterPosition)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onLongClick.invoke(holder.adapterPosition)
            true
        }
    }

    fun setNewData(newData: MutableList<UserItemAdapterArgument>) {
        val diffCallback = UserDiffUtilCallback(recycleList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recycleList.clear()
        recycleList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}
