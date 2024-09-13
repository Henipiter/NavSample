package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
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
        holder.binding.quantity.text = productList[position].quantity.toString()
        holder.binding.unitPrice.text = productList[position].unitPrice.toString()
        holder.binding.subtotalPrice.text = productList[position].subtotalPrice.toString()
        holder.binding.discountPrice.text = productList[position].discount.toString()
        holder.binding.finalPrice.text = productList[position].finalPrice.toString()
        holder.binding.productName.text = trimDescription(productList[position].name)
        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        holder.binding.boundary.setOnClickListener {
            if (holder.binding.additional.visibility == View.GONE) {
                holder.binding.additional.visibility = View.VISIBLE
                holder.binding.boundary.setBackgroundResource(R.drawable.arrow_drop_up)
            } else {
                holder.binding.additional.visibility = View.GONE
                holder.binding.boundary.setBackgroundResource(R.drawable.arrow_drop_down)
            }
        }

        validateParams(holder)
    }

    private fun validateParams(holder: MyViewHolder) {
        val doubleQuantity = trim(productList[position].quantity.toString()).toDoubleOrNull()
        val unitPrice = trim(productList[position].unitPrice.toString()).toDoubleOrNull()
        val subtotalPrice = trim(productList[position].subtotalPrice.toString()).toDoubleOrNull()
        val discount = trim(productList[position].discount.toString()).toDoubleOrNull()
        val finalPrice = trim(productList[position].finalPrice.toString()).toDoubleOrNull()

        if (doubleQuantity == null || unitPrice == null || subtotalPrice == null || (doubleQuantity * unitPrice * 100.0).roundToInt() / 100.0 != subtotalPrice) {
            holder.binding.subtotalPrice.setTextColor(Color.RED)
        } else {
            holder.binding.subtotalPrice.setTextColor(context.resources.getColor(R.color.basic_text_grey))
        }

        if (subtotalPrice == null || discount == null || finalPrice == null || subtotalPrice - discount != finalPrice) {
            holder.binding.finalPrice.setTextColor(Color.RED)
        } else {
            holder.binding.finalPrice.setTextColor(context.resources.getColor(R.color.basic_text_grey))

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
