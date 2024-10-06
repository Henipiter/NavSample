package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.databinding.StoreRowBinding
import com.example.navsample.entities.Store

class StoreListAdapter(
    var context: Context,
    var storeList: ArrayList<Store>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit
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
        if (storeList[position].nip != "") {
            holder.binding.nip.text = storeList[position].nip
        } else {
            holder.binding.nip.text = "---"

        }

        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        if (!isCorrectNIP(storeList[position].nip)) {
            holder.binding.nip.setTextColor(Color.RED)
        } else {
            holder.binding.nip.setTextColor(
                context.resources.getColor(
                    R.color.basic_text_grey,
                    context.theme
                )
            )
        }


    }


    private fun isCorrectNIP(valueNIP: String?): Boolean {
        if (valueNIP == null || !Regex("""[0-9]{10}""").matches(valueNIP)) {
            return false
        }
        val weight = arrayOf(6, 5, 7, 2, 3, 4, 5, 6, 7)
        var sum = 0
        for (i in 0..8) {
            sum += valueNIP[i].digitToInt() * weight[i]
        }
        return sum % 11 == valueNIP[9].digitToInt()
    }

    override fun getItemCount(): Int {
        return storeList.size
    }
}
