package com.example.navsample

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(

    var activity: Activity,
    var context: Context,
    var productList: ArrayList<Product>,
    var itemClickListener: ItemClickListener
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(
        var itemView: View,
        var prize: TextView = itemView.findViewById(R.id.prize),
        var productName: TextView = itemView.findViewById(R.id.product_name),
        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
    ) : RecyclerView.ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.receipt_row, parent, false)
        val view = inflater.inflate(R.layout.receipt_row, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.prize.text = productList[position].price.toString()
        holder.productName.text = productList[position].name?.let { trimDescription(it) }
        holder.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(productList[position])
        }
    }

    public interface ItemClickListener{
        public fun onItemClick(product: Product)
    }

    private fun trimDescription(description: String): String {
        if (!description.contains("\n") && description.length <= 16) {
            return description
        }
        var trimmedDescription = description.split("\n")[0]
        if (trimmedDescription.length > 16) {
            trimmedDescription = trimmedDescription.substring(0, 16)
        }
        return "$trimmedDescription..."
    }
    override fun getItemCount(): Int {
        return productList.size
    }
}
