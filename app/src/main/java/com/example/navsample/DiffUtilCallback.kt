package com.example.navsample

import androidx.recyclerview.widget.DiffUtil
import com.example.navsample.DTO.ExperimentalAdapterArgument

class DiffUtilCallback(
    private val oldList: List<ExperimentalAdapterArgument>,
    private val newList: List<ExperimentalAdapterArgument>
) :
    DiffUtil.Callback() {

    // old size
    override fun getOldListSize(): Int = oldList.size

    // new list size
    override fun getNewListSize(): Int = newList.size

    // if items are same
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem === newItem
    }

    // check if contents are same
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.color == newItem.color && oldItem.value == newItem.value
    }
}