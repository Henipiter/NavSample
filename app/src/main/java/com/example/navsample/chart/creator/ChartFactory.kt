package com.example.navsample.chart.creator

import android.graphics.Color
import com.example.navsample.chart.ChartColors
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.ColorTemplate

interface ChartFactory<
        ENTRY : Entry,
        DATASET : DataSet<ENTRY>,
        I_DATASET : IDataSet<ENTRY>,
        CHART_DATA : com.github.mikephil.charting.data.ChartData<I_DATASET>
        > {
    fun getTitle(): String

    fun getData(data: List<PriceByCategory>): CHART_DATA
    fun getSpecificDataSet(entries: List<ENTRY>): DATASET


    fun getColors(): List<Int> {
        val colors = ArrayList<Int>()
        for (color in ChartColors.COLORS) colors.add(Color.parseColor(color))
        colors.add(ColorTemplate.getHoloBlue())
        return colors
    }

}