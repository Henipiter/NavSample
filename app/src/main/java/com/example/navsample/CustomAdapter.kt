package com.example.navsample

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.ProductDTO
import kotlin.math.roundToInt

class CustomAdapter(
    var context: Context,
    var productList: ArrayList<ProductDTO>,
    var itemClickListener: ItemClickListener,
    var onFinish: (Int) -> Unit
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(
        var itemView: View,
        var ptuType: TextView = itemView.findViewById(R.id.ptu_type),
        var amount: TextView = itemView.findViewById(R.id.amount),
        var finalPrice: TextView = itemView.findViewById(R.id.final_prize),
        var itemPrice: TextView = itemView.findViewById(R.id.item_prize),
        var productName: TextView = itemView.findViewById(R.id.product_name),
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
        holder.ptuType.text = productList[position].ptuType.toString()
        holder.amount.text = productList[position].amount.toString()
        holder.itemPrice.text = productList[position].itemPrice.toString()
        holder.finalPrice.text = productList[position].finalPrice.toString()
        holder.productName.text = productList[position].name?.let { trimDescription(it) }
        holder.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(productList[position])
        }
        holder.mainLayout.setOnLongClickListener {
            Toast.makeText(context, "EE", Toast.LENGTH_SHORT).show()
            onFinish.invoke(position)
//            notifyItemRemoved(position)
            true
//            productList.remove(productList[position])
        }
        val floatAmount = trim(productList[position].amount.toString()).toDoubleOrNull()
        val itemPrice = trim(productList[position].itemPrice.toString()).toDoubleOrNull()
        val finalPrice = trim(productList[position].finalPrice.toString()).toDoubleOrNull()

        if (floatAmount == null || itemPrice == null || finalPrice == null || (floatAmount * itemPrice * 100.0).roundToInt() / 100.0 != finalPrice) {
            holder.finalPrice.setTextColor(Color.RED)
        }
    }

    private fun trim(x: String): String {
        var delimiter = false
        var newString = ""
        for (i in x) {
            if (i != '.' && !i.isDigit()) {
                return newString
            }
            if (delimiter && !i.isDigit()) {
                return newString
            }

            if (!delimiter && i == '.') {
                delimiter = true

            }
            newString += i
        }
        return newString
    }

    interface ItemClickListener {
        fun onItemClick(product: ProductDTO)
    }

    private fun trimDescription(description: String): String {
        val maxLength = 32
        if (!description.contains("\n") && description.length <= maxLength) {
            return description
        }
        var trimmedDescription = description.split("\n")[0]
        if (trimmedDescription.length > maxLength) {
            trimmedDescription = trimmedDescription.substring(0, maxLength)
        }
        return "$trimmedDescription..."
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
