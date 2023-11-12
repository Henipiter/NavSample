package com.example.navsample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.R

class ExperimentalListAdapter(
    var context: Context,
    var recycleList: ArrayList<ExperimentalAdapterArgument>,

    var onClick: (Int) -> Unit
) : RecyclerView.Adapter<ExperimentalListAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(
        var itemView: View,
        var textView: TextView = itemView.findViewById(R.id.text_view),

        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.row_experiment, parent, false)
        val view = inflater.inflate(R.layout.row_experiment, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.textView.text = recycleList[position].value
        holder.mainLayout.setBackgroundColor(recycleList[position].color)

        holder.mainLayout.setOnClickListener {
            onClick.invoke(position)

        }

    }

    override fun getItemCount(): Int {
        return recycleList.size
    }
}
