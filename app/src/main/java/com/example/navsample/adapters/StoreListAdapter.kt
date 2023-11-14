package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.StoreRowBinding
import com.example.navsample.entities.Store
import com.example.navsample.entities.relations.ReceiptWithStore

class StoreListAdapter(
    var context: Context,
    var storeList: ArrayList<Store>,
    var itemClickListener: ItemClickListener,
    var onDelete: (Int) -> Unit
) : RecyclerView.Adapter<StoreListAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(val binding: StoreRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoreRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.binding.storeName.text = storeList[position].name
        holder.binding.nip.text = storeList[position].nip

        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }


    }

    override fun getItemCount(): Int {
        return storeList.size
    }
}
