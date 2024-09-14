package com.example.navsample.adapters.sorting

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.example.navsample.R
import com.example.navsample.dto.SortingElementColor
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.sorting.AlgorithmDiffUtilCallback

class AlgorithmItemListAdapter(
    recycleList: ArrayList<AlgorithmItemAdapterArgument>,
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
    context: Context
) : SortingItemListAdapter<AlgorithmItemAdapterArgument>(recycleList) {

    private val blockedColor = getColor(context, R.color.background_color)
    private val defaultColor = getColor(context, R.color.sortingCardView)
    private val chosenColor = getColor(context, R.color.background_tile_chosen)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.textView.text = recycleList[holder.adapterPosition].value
        holder.binding.counter.text = recycleList[holder.adapterPosition].number.toString()

        when (recycleList[holder.adapterPosition].status) {
            Status.CHOSEN -> {
                holder.binding.mainLayout.setBackgroundColor(chosenColor)
                holder.binding.counter.visibility = View.VISIBLE
            }

            Status.DEFAULT -> {
                holder.binding.mainLayout.setBackgroundColor(defaultColor)
                holder.binding.counter.visibility = View.INVISIBLE
            }

            Status.BLOCKED -> {
                holder.binding.mainLayout.setBackgroundColor(blockedColor)
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

    fun setNewData(newData: MutableList<AlgorithmItemAdapterArgument>) {
        val diffCallback = AlgorithmDiffUtilCallback(recycleList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recycleList.clear()
        recycleList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getColor(context: Context, property: Int): Int {
        return context.resources.getColor(property, context.theme)
    }

}
