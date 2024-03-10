package com.example.navsample.chart.data

import android.graphics.Color
import com.example.navsample.chart.ChartData
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet

interface RadialDataFactory<
        ENTRY : Entry,
        DATASET : DataSet<ENTRY>,
        I_DATASET : IDataSet<ENTRY>,
        CHART_DATA : com.github.mikephil.charting.data.ChartData<I_DATASET>
        > : ChartDataFactory<ENTRY, DATASET, I_DATASET, CHART_DATA> {

    fun getLegend(data: List<PriceByCategory>): List<String>
    fun getSpecificEntry(value: Float, label: String): ENTRY
    fun getSpecificChartData(dataset: DATASET): CHART_DATA
    fun getRadialEntries(dataset: List<ChartData>): List<ENTRY> {
        val entries = ArrayList<ENTRY>()
        dataset.forEach {
            entries.add(getSpecificEntry(it.value, it.label))
        }
        return entries
    }

    fun getDataSetTemplate(entries: List<ENTRY>): DATASET {
        val dataSet = getSpecificDataSet(entries)
        dataSet.setDrawIcons(false)
        return dataSet
    }

    fun getDataTemplate(dataset: DATASET): CHART_DATA {
        val data = getSpecificChartData(dataset)

        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        return data
    }

    fun createCategoryData(data: List<PriceByCategory>): List<ChartData> {
        val categoryData: MutableList<ChartData> = mutableListOf()
        data.forEach {
            categoryData.add(ChartData(it.category, it.price))
        }
        return categoryData
    }
}
