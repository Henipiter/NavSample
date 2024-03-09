package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.CategoryRowBinding
import com.example.navsample.entities.Category

class CategoryListAdapter(
    var context: Context,
    var categoryList: ArrayList<Category>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {
    var position = 0


    class MyViewHolder(val binding: CategoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.binding.categoryName.text = categoryList[position].name
        try {
            holder.binding.colorSquare.setBackgroundColor(Color.parseColor(categoryList[position].color))
        } catch (e: Exception) {
            Log.e(
                "CategoryListAdapter",
                "cannot parse category color" + categoryList[position].color,
            )
        }

        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }
}
