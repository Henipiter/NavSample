package com.example.navsample.chart.creator


import android.graphics.Color
import android.graphics.Paint
import com.example.navsample.chart.MyXAxisFormatter
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet

class RadarChartCreator :
    ChartFactory<RadarEntry, IRadarDataSet, RadarData, RadarChart> {

    override fun initializeChart(chart: RadarChart) {
    }

    override fun drawChart(chart: RadarChart, data: RadarData, legend: List<String>) {
        data.setValueTextColor(Color.WHITE)
        chart.data = data
        chart.description.isEnabled = false
        chart.legend.textColor = Color.WHITE
        chart.legend.form = Legend.LegendForm.CIRCLE
        chart.webColorInner = Color.WHITE
        chart.webColor = Color.WHITE
        chart.xAxis.valueFormatter = MyXAxisFormatter(legend)
        chart.xAxis.valueFormatter
        chart.yAxis.textColor = Color.WHITE
        chart.yAxis.axisLineColor = Color.WHITE
        chart.renderer.paintRender.style = Paint.Style.STROKE;
        chart.highlightValues(null)
        chart.invalidate()
    }
}
