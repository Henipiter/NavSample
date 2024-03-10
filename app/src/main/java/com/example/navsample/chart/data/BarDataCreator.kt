package com.example.navsample.chart.data


import android.graphics.Color
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class BarDataCreator : LinearDataFactory<BarEntry, BarDataSet, IBarDataSet, BarData> {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time
    private var today = dateFormat.format(Date())
    private var ago = dateFormat.format(threeMonthsAgo)

    override fun getTitle(): String {
        return "Bar Chart"
    }

    override fun getData(data: List<PriceByCategory>): BarData {
        val categories = data.map { it.category }.toSortedSet()
        if (categories.size == 0) {
            categories.add("")
        }

        val timelineData = LinearDataFactory.createTimelineData(ago, today, data)
        val dataSets = convertToChart(categories, timelineData)

        val barData = getSpecificChartData(dataSets)
        barData.setValueTextColor(Color.WHITE)
        return barData
    }

    override fun getLegend(): List<String> {
        return getLegend(ago, today)
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

    override fun setDateRange(ago: String, currentDate: String) {
        this.ago = ago
        this.today = currentDate
    }

    override fun createDataSet(
        values: List<BarEntry>, categories: Set<String>, id: Int
    ): IBarDataSet {
        val dataSet = BarDataSet(values, categories.toList()[id])
        dataSet.valueTextColor = Color.WHITE
        dataSet.color = getChartColor(id)
        return dataSet
    }

}
