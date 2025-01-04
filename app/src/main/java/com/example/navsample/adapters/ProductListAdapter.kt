package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.databinding.ProductDtoRowBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.PriceUtils.Companion.intQuantityToString
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.Tag
import com.google.android.flexbox.FlexboxLayout

class ProductListAdapter(
    var context: Context,
    var productList: MutableList<Product>,
    var tagList: List<List<Tag>>,
    var categoryList: List<Category>,
    private var itemClickListener: ItemClickListener,
    private var onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<ProductListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ProductDtoRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ProductDtoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        setTexts(binding, position)
        setColorOfPriceInfo(binding, position)

        binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        binding.mainLayout.setOnLongClickListener {
            onDelete.invoke(position)
            true
        }
        addTagsToFlexbox(holder, position)
    }

    private fun setTexts(binding: ProductDtoRowBinding, position: Int) {
        setDiscountText(binding, position)
        binding.ptuType.text = productList[position].ptuType
        binding.quantity.text = intQuantityToString(productList[position].quantity)
        binding.unitPrice.text = intPriceToString(productList[position].unitPrice)
        binding.finalPrice.text = intPriceToString(productList[position].finalPrice)
        binding.productName.text = trimDescription(productList[position].name)
        setCategoryInfo(binding, position)
    }

    private fun setCategoryInfo(binding: ProductDtoRowBinding, position: Int) {
        val category = matchCategory(position)
        binding.categoryName.text = category.name
        binding.categoryColor.setBackgroundColor(ColorManager.parseColor(category.color))
    }

    private fun matchCategory(position: Int): Category {
        return try {
            categoryList.first { it.id == productList[position].categoryId }
        } catch (exception: Exception) {
            Category("-", "#FFFFFF")
        }
    }

    private fun setDiscountText(binding: ProductDtoRowBinding, position: Int) {
        if (productList[position].discount != 0) {
            binding.discountPrice.visibility = View.VISIBLE
            binding.minusSign.visibility = View.VISIBLE
            binding.discountPrice.text = intPriceToString(productList[position].discount)
        } else {
            binding.discountPrice.visibility = View.GONE
            binding.minusSign.visibility = View.GONE
        }
    }

    private fun setColorOfPriceInfo(binding: ProductDtoRowBinding, position: Int) {
        if (!productList[position].validPrice) {
            binding.finalPrice.setTextColor(ColorManager.getWrongColor())
        } else {
            binding.finalPrice.setTextColor(ColorManager.getNormalColor(context))
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


    private fun addTagsToFlexbox(holder: MyViewHolder, position: Int) {
        holder.binding.flexboxLayout.removeAllViews()
        tagList[position].forEach {
            val textView = createTextView(holder, it.name)
            holder.binding.flexboxLayout.addView(textView)
        }
    }

    private fun createTextView(holder: MyViewHolder, text: String): TextView {
        val layoutInflater = LayoutInflater.from(holder.itemView.context)
        val textView =
            layoutInflater.inflate(
                R.layout.tag_item_layout,
                holder.binding.flexboxLayout,
                false
            ) as TextView

        textView.apply {
            this.text = text
            setPadding(16, 8, 16, 8)
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 8
                bottomMargin = 8
            }
        }
        return textView
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
