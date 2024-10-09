package com.example.navsample.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.ProductDtoRowBinding
import com.example.navsample.dto.Utils.Companion.doubleToString
import com.example.navsample.dto.Utils.Companion.quantityToString
import com.example.navsample.dto.Utils.Companion.roundDouble
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product

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
        } catch (exception: Exception) {
            categoryList[0]
        }
        if (roundDouble(productList[position].discount) != 0.0) {
            holder.binding.discountPrice.visibility = View.VISIBLE
            holder.binding.minusSign.visibility = View.VISIBLE
            holder.binding.discountPrice.text = doubleToString(productList[position].discount)
        } else {
            holder.binding.discountPrice.visibility = View.GONE
            holder.binding.minusSign.visibility = View.GONE
        }
        holder.binding.ptuType.text = productList[position].ptuType
        holder.binding.quantity.text = quantityToString(productList[position].quantity)
        holder.binding.unitPrice.text = doubleToString(productList[position].unitPrice)
        holder.binding.finalPrice.text = doubleToString(productList[position].finalPrice)
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
        if (roundDouble(productList[position].quantity * productList[position].unitPrice) != productList[position].subtotalPrice ||
            roundDouble(productList[position].subtotalPrice - productList[position].discount) != productList[position].finalPrice
        ) {
            holder.binding.finalPrice.setTextColor(Color.RED)
        }
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
