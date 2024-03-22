package com.example.navsample

import androidx.recyclerview.widget.DiffUtil
import com.example.navsample.dto.AlgorithmItemAdapterArgument

class AlgorithmDiffUtilCallback(
    private val oldList: List<AlgorithmItemAdapterArgument>,
    private val newList: List<AlgorithmItemAdapterArgument>,
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

        return oldItem.status == newItem.status && oldItem.value == newItem.value && oldItem.number == newItem.number
    }
}