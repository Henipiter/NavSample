package com.example.navsample.chart.creator


import com.example.navsample.chart.MyXAxisFormatter
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet

class RadarChartCreator :
    ChartFactory<RadarEntry, IRadarDataSet, RadarData, RadarChart> {

    override fun initializeChart(chart: RadarChart) {
    }

    override fun drawChart(chart: RadarChart, data: RadarData, legend: List<String>) {
        chart.data = data
        chart.xAxis.valueFormatter = MyXAxisFormatter(legend)
        chart.highlightValues(null)
        chart.invalidate()
    }
}
