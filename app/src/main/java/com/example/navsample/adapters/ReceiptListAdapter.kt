package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.entities.relations.ReceiptWithStore

class ReceiptListAdapter(
    var context: Context,
    var receiptList: ArrayList<ReceiptWithStore>,
    var itemClickListener: ItemClickListener,
    var onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ReceiptListAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(
        var itemView: View,
        var storeName: TextView = itemView.findViewById(R.id.store_name),
        var date: TextView = itemView.findViewById(R.id.nip),
        var pln: TextView = itemView.findViewById(R.id.final_prize),
        var time: TextView = itemView.findViewById(R.id.time),
        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.receipt_row, parent, false)
        val view = inflater.inflate(R.layout.receipt_row, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.storeName.text = receiptList[position].name
        holder.date.text = receiptList[position].date
        holder.time.text = receiptList[position].time
        holder.pln.text = receiptList[position].pln.toString()
        holder.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }
}
