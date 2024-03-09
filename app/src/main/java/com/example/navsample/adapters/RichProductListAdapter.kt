package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.ProductRowBinding
import com.example.navsample.entities.relations.ProductRichData
import kotlin.math.roundToInt

class RichProductListAdapter(
    var context: Context,
    var productList: ArrayList<ProductRichData>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<RichProductListAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(val binding: ProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.binding.storeName.text = productList[position].storeName
        holder.binding.receiptDate.text = productList[position].date
        holder.binding.categoryName.text = productList[position].categoryName
        holder.binding.categoryColor.setBackgroundColor(Color.parseColor(productList[position].categoryColor))
        holder.binding.ptuType.text = productList[position].ptuType
        holder.binding.amount.text = productList[position].amount.toString()
        holder.binding.itemPrice.text = productList[position].itemPrice.toString()
        holder.binding.finalPrice.text = productList[position].finalPrice.toString()
        holder.binding.productName.text = trimDescription(productList[position].name)
        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        val floatAmount = trim(productList[position].amount.toString()).toDoubleOrNull()
        val itemPrice = trim(productList[position].itemPrice.toString()).toDoubleOrNull()
        val finalPrice = trim(productList[position].finalPrice.toString()).toDoubleOrNull()

        if (floatAmount == null || itemPrice == null || finalPrice == null || (floatAmount * itemPrice * 100.0).roundToInt() / 100.0 != finalPrice) {
            holder.binding.finalPrice.setTextColor(Color.RED)
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
