package com.example.navsample.chart.creator


import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet

class PieChartCreator : ChartFactory<PieEntry, IPieDataSet, PieData, PieChart> {

    override fun initializeChart(chart: PieChart) {
        chart.setUsePercentValues(false)
        chart.description.isEnabled = false
        chart.setExtraOffsets(5F, 10F, 5F, 5F)
        chart.transparentCircleRadius = 21f
        chart.holeRadius = 16f

        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.setDrawInside(false)
        chart.legend.isWordWrapEnabled = true
        chart.legend.xEntrySpace = 7f
        chart.legend.yEntrySpace = 0f
        chart.legend.yOffset = 0f
        chart.legend.textColor = Color.WHITE
        chart.legend.form = Legend.LegendForm.CIRCLE

        chart.setEntryLabelColor(Color.WHITE)
        chart.setEntryLabelTextSize(12f)
    }

    override fun drawChart(chart: PieChart, data: PieData, legend: List<String>) {
        chart.data = data
        chart.highlightValues(null)
        chart.invalidate()
    }
}
