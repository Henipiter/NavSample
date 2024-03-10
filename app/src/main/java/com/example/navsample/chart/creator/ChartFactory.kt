package com.example.navsample.chart.creator

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet

interface ChartFactory<
        ENTRY : Entry,
        I_DATASET : IDataSet<ENTRY>,
        CHART_DATA : com.github.mikephil.charting.data.ChartData<I_DATASET>,
        CHART : ChartInterface
        > {

    fun initializeChart(chart: CHART)
    fun drawChart(chart: CHART, data: CHART_DATA)

}