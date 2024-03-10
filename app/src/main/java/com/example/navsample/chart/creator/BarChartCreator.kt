package com.example.navsample.chart.creator


import android.graphics.Color
import com.example.navsample.chart.MyXAxisFormatter
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

class BarChartCreator(
    private var ago: String,
    private var today: String
) : LinearChartFactory<BarEntry, BarDataSet, IBarDataSet, BarData, BarChart> {
    override fun getTitle(): String {
        return "Bar Chart"
    }

    override fun getData(data: List<PriceByCategory>): BarData {
        val categories = data.map { it.category }.toSortedSet()
        if (categories.size == 0) {
            categories.add("")
        }

        val timelineData = LinearChartFactory.createTimelineData(ago, today, data)
        val dataSets = convertToChart(categories, timelineData)

        val barData = getSpecificChartData(dataSets)
        barData.setValueTextColor(Color.WHITE)
        return barData
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

    override fun drawChart(chart: BarChart, data: BarData) {
        val groupSpace = 0.1f
        val barSpace = 0.01f
        val barWidth = 0.23f

        chart.setDrawValueAboveBar(true)
        chart.data = data

        chart.barData.barWidth = barWidth
        chart.xAxis.valueFormatter = MyXAxisFormatter(getDateLegend(ago, today))
        chart.xAxis.setCenterAxisLabels(true)
        chart.setVisibleXRangeMaximum(4F)
        chart.moveViewToX(1F)

        try {
            chart.groupBars(1F, groupSpace, barSpace)
        } catch (e: Exception) {
        }
        chart.invalidate()
    }
}
