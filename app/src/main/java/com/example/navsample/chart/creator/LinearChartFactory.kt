package com.example.navsample.chart.creator

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface LinearChartFactory<
        ENTRY : Entry,
        I_DATASET : IDataSet<ENTRY>,
        CHART_DATA : com.github.mikephil.charting.data.ChartData<I_DATASET>,
        CHART : ChartInterface
        > : ChartFactory<ENTRY, I_DATASET, CHART_DATA, CHART> {

    fun setDateRange(ago: String, currentDate: String)

    fun getDateLegend(fromDate: String, toDate: String): List<String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(fromDate, formatter)
        val endDate = LocalDate.parse(toDate, formatter)
        val dataList = mutableListOf<String>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            dataList.add(currentDate.format(formatter).substring(0, 7))
            currentDate = currentDate.plusMonths(1)
        }
        return dataList
    }

}