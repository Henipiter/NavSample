package com.example.navsample.chart.creator


import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet

class RadarChartCreator :
    RadialChartFactory<RadarEntry, RadarDataSet, IRadarDataSet, RadarData, RadarChart> {
    override fun getTitle(): String {
        return "Radar Chart"
    }

    override fun getData(data: List<PriceByCategory>): RadarData {
        val categoryData = createCategoryData(data)
        val entries = getRadialEntries(categoryData)
        val dataSet: RadarDataSet = getDataSetTemplate(entries)
        return getDataTemplate(dataSet)
    }

    override fun getSpecificDataSet(entries: List<RadarEntry>): RadarDataSet {
        return RadarDataSet(entries, getTitle())
    }

    override fun getSpecificEntry(value: Float, label: String): RadarEntry {
        return RadarEntry(value, label)
    }

    override fun getSpecificChartData(dataset: RadarDataSet): RadarData {
        return RadarData(dataset)
    }

    override fun drawChart(chart: RadarChart, data: RadarData) {
        chart.data = data
        chart.highlightValues(null)
        chart.invalidate()
    }
}
