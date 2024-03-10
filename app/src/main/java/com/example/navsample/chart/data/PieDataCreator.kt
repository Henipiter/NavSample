package com.example.navsample.chart.data


import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet

class PieDataCreator : RadialDataFactory<PieEntry, PieDataSet, IPieDataSet, PieData> {
    override fun getTitle(): String {
        return "Pie Chart"
    }

    override fun getData(data: List<PriceByCategory>): PieData {
        val categoryData = createCategoryData(data)
        val entries = getRadialEntries(categoryData)
        val dataSet = getDataSetTemplate(entries)
        return getDataTemplate(dataSet)
    }

    override fun getSpecificDataSet(entries: List<PieEntry>): PieDataSet {
        return PieDataSet(entries, getTitle())
    }

    override fun getSpecificEntry(value: Float, label: String): PieEntry {
        return PieEntry(value, label)
    }

    override fun getSpecificChartData(dataset: PieDataSet): PieData {
        return PieData(dataset)
    }


}
