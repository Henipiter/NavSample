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
import com.example.navsample.dto.Utils.Companion.doubleToString
import com.example.navsample.dto.Utils.Companion.quantityToString
import com.example.navsample.entities.relations.ProductRichData


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
        holder.binding.quantity.text = quantityToString(productList[position].quantity)
        holder.binding.unitPrice.text = doubleToString(productList[position].unitPrice)
        holder.binding.subtotalPrice.text = doubleToString(productList[position].subtotalPrice)
        holder.binding.discountPrice.text = doubleToString(productList[position].discount)
        holder.binding.finalPrice.text = doubleToString(productList[position].finalPrice)
        holder.binding.productName.text = trimDescription(productList[position].name)
        setCollapseOrExpand(holder, position)
        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        holder.binding.boundaryButton.setOnClickListener {
            val isCollapse = productList[position].collapse
            productList[position].collapse = !isCollapse
            setCollapseOrExpand(holder, position)
            this.notifyItemChanged(position)
        }
        if (!productList[position].validPrice) {
            holder.binding.finalPrice.setTextColor(Color.RED)
        } else {
            holder.binding.finalPrice.setTextColor(
                context.resources.getColor(R.color.basic_text_grey, context.theme)
            )

        }
    }

    private fun setCollapseOrExpand(holder: MyViewHolder, position: Int) {
        if (productList[position].collapse) {
            collapse(holder)
        } else {
            expand(holder)
        }
    }

    private fun collapse(holder: MyViewHolder) {
        holder.binding.additional.visibility = View.GONE
        holder.binding.boundary.setBackgroundResource(R.drawable.arrow_drop_down)
    }

    private fun expand(holder: MyViewHolder) {
        holder.binding.additional.visibility = View.VISIBLE
        holder.binding.boundary.setBackgroundResource(R.drawable.arrow_drop_up)

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
