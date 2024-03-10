package com.example.navsample.chart.data


import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet

class RadarDataCreator :
    RadialDataFactory<RadarEntry, RadarDataSet, IRadarDataSet, RadarData> {
    override fun getTitle(): String {
        return "Radar Chart"
    }

    override fun getData(data: List<PriceByCategory>): RadarData {
        val categoryData = createCategoryData(data)
        val entries = getRadialEntries(categoryData)
        val dataSet: RadarDataSet = getDataSetTemplate(entries)
        dataSet.color = getColors()[0]
        return getDataTemplate(dataSet)
    }

    override fun getLegend(data: List<PriceByCategory>): List<String> {
        return data.map { it.category }
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

}
