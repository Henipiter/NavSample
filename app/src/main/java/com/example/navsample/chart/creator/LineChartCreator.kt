package com.example.navsample.chart.creator


import android.graphics.Color
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class LineChartCreator(
    private var ago: String,
    private var today: String,
    private var it: ArrayList<PriceByCategory>
) : LinearChartFactory<Entry, LineDataSet, ILineDataSet, LineData> {
    override fun getTitle(): String {
        return "Line Chart"
    }

    override fun getData(data: List<PriceByCategory>): LineData {
        val categories = it.map { it.category }.toSortedSet()
        val timelineData = LinearChartFactory.createTimelineData(ago, today, it)
        val dataSets = convertToChart(categories, timelineData)
        return getSpecificChartData(dataSets)
    }

    override fun getSpecificDataSet(entries: List<Entry>): LineDataSet {
        return LineDataSet(entries, getTitle())
    }

    override fun getSpecificEntry(x: Float, y: Float): Entry {
        return Entry(x, y)
    }

    override fun getSpecificChartData(dataset: List<ILineDataSet>): LineData {
        return LineData(dataset)
    }

    override fun createDataSet(
        values: List<Entry>,
        categories: Set<String>,
        id: Int
    ): ILineDataSet {

        val dataSet = LineDataSet(values, categories.toList()[id])
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 4f
        dataSet.valueTextColor = Color.WHITE

        val color = getChartColor(id)
        dataSet.color = color
        dataSet.setCircleColor(color)
        return dataSet
    }

}
