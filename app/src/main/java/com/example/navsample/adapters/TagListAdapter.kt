package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.TagRowBinding
import com.example.navsample.entities.database.Tag

class TagListAdapter(
    var context: Context,
    var tagList: ArrayList<Tag>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit
) : RecyclerView.Adapter<TagListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: TagRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = TagRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    private fun setTexts(binding: TagRowBinding, position: Int) {
        binding.tagName.text = tagList[position].name
    }


    override fun getItemCount(): Int {
        return tagList.size
    }
}
