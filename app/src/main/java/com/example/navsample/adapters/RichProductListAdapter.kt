package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.databinding.ProductRowBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.PriceUtils.Companion.intQuantityToString
import com.example.navsample.entities.relations.ProductRichData


class RichProductListAdapter(
    var context: Context,
    var productList: ArrayList<ProductRichData>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<RichProductListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        setTexts(binding, position)
        setCollapseOrExpandTile(binding, position)
        setColorOfPriceInfo(binding, position)

        binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        binding.finalPriceCard.setOnClickListener {
            val isCollapse = productList[position].collapse
            productList[position].collapse = !isCollapse
            setCollapseOrExpandTile(binding, position)
            this.notifyItemChanged(position)
        }
    }

    private fun setTexts(binding: ProductRowBinding, position: Int) {
        binding.storeName.text = productList[position].storeName
        binding.receiptDate.text = productList[position].date
        binding.categoryName.text = productList[position].categoryName
        binding.categoryColor.setBackgroundColor(ColorManager.parseColor(productList[position].categoryColor))
        binding.ptuType.text = productList[position].ptuType
        binding.quantity.text = intQuantityToString(productList[position].quantity)
        binding.unitPrice.text = intPriceToString(productList[position].unitPrice)
        binding.subtotalPrice.text = intPriceToString(productList[position].subtotalPrice)
        binding.discountPrice.text = intPriceToString(productList[position].discount)
        binding.finalPrice.text = intPriceToString(productList[position].finalPrice)
        binding.productName.text = trimDescription(productList[position].name)

    }

    private fun setColorOfPriceInfo(binding: ProductRowBinding, position: Int) {
        if (!productList[position].validPrice) {
            binding.finalPrice.setTextColor(ColorManager.getWrongColor())
        } else {
            binding.finalPrice.setTextColor(ColorManager.getNormalColor(context))
        }
    }

    private fun setCollapseOrExpandTile(binding: ProductRowBinding, position: Int) {
        if (productList[position].collapse) {
            collapseTile(binding)
        } else {
            expandTile(binding)
        }
    }

    private fun collapseTile(binding: ProductRowBinding) {
        binding.additional.visibility = View.GONE
        binding.boundary.setBackgroundResource(R.drawable.arrow_drop_down)
    }

    private fun expandTile(binding: ProductRowBinding) {
        binding.additional.visibility = View.VISIBLE
        binding.boundary.setBackgroundResource(R.drawable.arrow_drop_up)

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
