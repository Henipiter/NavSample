package com.example.navsample.sorting

import androidx.recyclerview.widget.DiffUtil
import com.example.navsample.dto.UserItemAdapterArgument

class UserDiffUtilCallback(
    private val oldList: List<UserItemAdapterArgument>,
    private val newList: List<UserItemAdapterArgument>,
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

        return oldItem.value == newItem.value && oldItem.type == newItem.type
    }
}