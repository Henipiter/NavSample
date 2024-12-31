package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.CategoryRowBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.entities.database.Category

class CategoryListAdapter(
    var context: Context,
    var categoryList: ArrayList<Category>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {


    class MyViewHolder(val binding: CategoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        setTexts(binding, position)

        binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
    }

    private fun setTexts(binding: CategoryRowBinding, position: Int) {
        binding.categoryName.text = categoryList[position].name
        binding.colorSquare.setBackgroundColor(ColorManager.parseColor(categoryList[position].color))
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }
}
