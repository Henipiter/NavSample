package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.ItemClickListener
import com.example.navsample.databinding.ReceiptRowBinding
import com.example.navsample.dto.ColorManager
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.entities.relations.ReceiptWithStore

class ReceiptListAdapter(
    var context: Context,
    var receiptList: ArrayList<ReceiptWithStore>,
    private var itemClickListener: ItemClickListener,
    private var onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<ReceiptListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ReceiptRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ReceiptRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        setTexts(binding, position)
        setColorOfPriceInfo(binding, position)
        setColorOfProductInfo(binding, position)

        holder.binding.mainLayout.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.binding.mainLayout.setOnLongClickListener {
            onLongClick.invoke(position)
            true
        }
    }

    private fun setTexts(binding: ReceiptRowBinding, position: Int) {
        binding.storeName.text = receiptList[position].name
        binding.date.text = receiptList[position].date
        binding.time.text = receiptList[position].time
        binding.subtotalPrize.text = intPriceToString(receiptList[position].pln)
        binding.productCount.text = receiptList[position].productCount.toString()
    }

    private fun setColorOfPriceInfo(binding: ReceiptRowBinding, position: Int) {
        if (receiptList[position].productPriceSum != receiptList[position].pln) {
            binding.subtotalPrize.setTextColor(ColorManager.getWrongColor())
            binding.pln.setTextColor(ColorManager.getWrongColor())
        } else {
            binding.subtotalPrize.setTextColor(ColorManager.getNormalColor(context))
            binding.pln.setTextColor(ColorManager.getNormalColor(context))
        }
    }

    private fun setColorOfProductInfo(binding: ReceiptRowBinding, position: Int) {
        if (receiptList[position].validProductCount != receiptList[position].productCount) {
            binding.productCount.setTextColor(ColorManager.getWrongColor())
            binding.productText.setTextColor(ColorManager.getWrongColor())
        } else {
            binding.productCount.setTextColor(ColorManager.getNormalColor(context))
            binding.productText.setTextColor(ColorManager.getNormalColor(context))
        }
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }
}
