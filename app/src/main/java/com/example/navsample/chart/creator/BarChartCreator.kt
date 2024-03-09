package com.example.navsample.chart.creator


import android.graphics.Color
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

class BarChartCreator(
    private var ago: String,
    private var today: String,
    private var it: ArrayList<PriceByCategory>
) : LinearChartFactory<BarEntry, BarDataSet, IBarDataSet, BarData> {
    override fun getTitle(): String {
        return "Bar Chart"
    }

    override fun getData(data: List<PriceByCategory>): BarData {
        val categories = it.map { it.category }.toSortedSet()
        val timelineData = LinearChartFactory.createTimelineData(ago, today, it)
        val dataSets = convertToChart(categories, timelineData)
        return getSpecificChartData(dataSets)
    }

    override fun getSpecificDataSet(entries: List<BarEntry>): BarDataSet {
        return BarDataSet(entries, getTitle())
    }

    override fun getSpecificEntry(x: Float, y: Float): BarEntry {
        return BarEntry(x, y)
    }

    override fun getSpecificChartData(dataset: List<IBarDataSet>): BarData {
        return BarData(dataset)
    }

    override fun createDataSet(
        values: List<BarEntry>,
        categories: Set<String>,
        id: Int
    ): IBarDataSet {
        val dataSet = BarDataSet(values, categories.toList()[id])
        dataSet.valueTextColor = Color.WHITE
        dataSet.color = getChartColor(id)
        return dataSet
    }
}
