package com.example.navsample.chart.creator


import com.example.navsample.chart.MyXAxisFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet


class BarChartCreator : ChartFactory<BarEntry, IBarDataSet, BarData, BarChart> {
    override fun initializeChart(chart: BarChart) {
    }

    override fun drawChart(chart: BarChart, data: BarData, legend: List<String>) {
        val groupSpace = 0.1f
        val barSpace = 0.01f
        val barWidth = 0.23f

        chart.setDrawValueAboveBar(true)
        chart.data = data

        chart.barData.barWidth = barWidth
        chart.xAxis.valueFormatter = MyXAxisFormatter(legend)
        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.disableGridDashedLine()

        chart.setDrawGridBackground(false);
        chart.setVisibleXRangeMaximum(4F)
        chart.moveViewToX(1F)
        chart.axisLeft.setDrawGridLines(false)

        chart.axisRight.isEnabled = false
        chart.axisLeft.isEnabled = false
        try {
            chart.groupBars(1F, groupSpace, barSpace)
        } catch (_: Exception) {
        }
        chart.invalidate()
    }
}
