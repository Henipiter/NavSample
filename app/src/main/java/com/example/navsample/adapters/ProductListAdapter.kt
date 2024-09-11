package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.ProductDtoRowBinding
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import kotlin.math.roundToInt

class ProductListAdapter(
    var context: Context,
    var productList: ArrayList<Product>,
    var categoryList: ArrayList<Category>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<ProductListAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(val binding: ProductDtoRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ProductDtoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        val category = try {
            categoryList.first { it.id == productList[position].categoryId }
        } catch (e: Exception) {
            categoryList[0]
        }

        holder.binding.ptuType.text = productList[position].ptuType
        holder.binding.quantity.text = productList[position].quantity.toString()
        holder.binding.unitPrice.text = productList[position].unitPrice.toString()
        holder.binding.subtotalPrice.text = productList[position].subtotalPrice.toString()
        holder.binding.finalPrice.text = productList[position].finalPrice.toString()
        holder.binding.discountPrice.text = productList[position].discount.toString()
        holder.binding.storeName.text = productList[position].raw
        holder.binding.receiptDate.text = ""
        holder.binding.categoryName.text = category.name
        holder.binding.categoryColor.setBackgroundColor(Color.parseColor(category.color))
        holder.binding.productName.text = trimDescription(productList[position].name)
        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        val DoubleQuantity = trim(productList[position].quantity.toString()).toDoubleOrNull()
        val unitPrice = trim(productList[position].unitPrice.toString()).toDoubleOrNull()
        val subtotalPrice = trim(productList[position].subtotalPrice.toString()).toDoubleOrNull()

        if (DoubleQuantity == null || unitPrice == null || subtotalPrice == null || (DoubleQuantity * unitPrice * 100.0).roundToInt() / 100.0 != subtotalPrice) {
            holder.binding.subtotalPrice.setTextColor(Color.RED)
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
