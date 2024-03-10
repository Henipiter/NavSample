package com.example.navsample.chart.creator


import com.example.navsample.chart.MyXAxisFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class BarChartCreator : LinearChartFactory<BarEntry, IBarDataSet, BarData, BarChart> {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time
    private var today = dateFormat.format(Date())
    private var ago = dateFormat.format(threeMonthsAgo)

    override fun setDateRange(ago: String, currentDate: String) {
        this.ago = ago
        this.today = currentDate
    }

    override fun initializeChart(chart: BarChart) {
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
        } catch (_: Exception) {
        }
        chart.invalidate()
    }
}
