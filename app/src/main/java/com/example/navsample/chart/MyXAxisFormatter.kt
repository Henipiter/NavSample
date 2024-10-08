package com.example.navsample.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MyXAxisFormatter(
    private var xAxis: List<String>,
) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return xAxis.getOrNull(value.toInt()) ?: value.toString()
    }
}