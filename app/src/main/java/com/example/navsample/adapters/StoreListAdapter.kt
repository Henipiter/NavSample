package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.StoreRowBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.dto.NipValidator
import com.example.navsample.entities.database.Store

class StoreListAdapter(
    var context: Context,
    var storeList: ArrayList<Store>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit
) : RecyclerView.Adapter<StoreListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: StoreRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoreRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        setTexts(binding, position)
        setColorOfNIPInfo(binding, position)

        binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
    }

    private fun setTexts(binding: StoreRowBinding, position: Int) {
        binding.storeName.text = storeList[position].name
        setNipText(binding, position)
    }

    private fun setNipText(binding: StoreRowBinding, position: Int) {
        if (storeList[position].nip != "") {
            binding.nip.text = storeList[position].nip
        } else {
            binding.nip.text = "---"
        }
    }

    private fun setColorOfNIPInfo(binding: StoreRowBinding, position: Int) {
        if (!NipValidator.validate(storeList[position].nip)) {
            binding.nip.setTextColor(ColorManager.getWrongColor())
        } else {
            binding.nip.setTextColor(ColorManager.getNormalColor(context))
        }
    }

    override fun getItemCount(): Int {
        return storeList.size
    }
}
