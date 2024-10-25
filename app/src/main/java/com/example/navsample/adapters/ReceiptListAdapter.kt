package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.ReceiptRowBinding
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.entities.relations.ReceiptWithStore

class ReceiptListAdapter(
    var context: Context,
    var receiptList: ArrayList<ReceiptWithStore>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ReceiptListAdapter.MyViewHolder>() {
    var position = 0


    class MyViewHolder(val binding: ReceiptRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ReceiptRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.binding.storeName.text = receiptList[position].name
        holder.binding.date.text = receiptList[position].date
        holder.binding.time.text = receiptList[position].time
        holder.binding.subtotalPrize.text = intPriceToString(receiptList[position].pln)
        holder.binding.productCount.text = receiptList[position].productCount.toString()

        if (receiptList[position].validProductCount != receiptList[position].productCount) {
            holder.binding.productCount.setTextColor(Color.RED)
            holder.binding.productText.setTextColor(Color.RED)
        }
        if (!receiptList[position].validPriceSum) {
            holder.binding.subtotalPrize.setTextColor(Color.RED)
            holder.binding.pln.setTextColor(Color.RED)
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
        return receiptList.size
    }
}
