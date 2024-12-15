package com.example.navsample.chart.data

import android.graphics.Color
import com.example.navsample.chart.ChartColors
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.SortedSet

interface LinearDataFactory<
        ENTRY : Entry,
        DATASET : DataSet<ENTRY>,
        I_DATASET : IDataSet<ENTRY>,
        CHART_DATA : com.github.mikephil.charting.data.ChartData<I_DATASET>

        > : ChartDataFactory<ENTRY, DATASET, I_DATASET, CHART_DATA> {

    fun setDateRange(ago: String, currentDate: String)
    fun getSpecificEntry(x: Float, y: Float): ENTRY
    fun getSpecificChartData(dataset: List<I_DATASET>): CHART_DATA
    fun createDataSet(values: List<ENTRY>, categories: Set<String>, id: Int): I_DATASET
    fun convertToChart(
        categories: SortedSet<String>,
        array: List<List<Float>>,
    ): List<I_DATASET> {
        val dataSets = ArrayList<I_DATASET>()
        array.forEachIndexed { id, categoryList ->
            val values = getChartDataEntries(categoryList)
            val dataSet = createDataSet(values, categories, id)
            dataSets.add(dataSet)
        }
        return dataSets
    }

    fun getChartColor(id: Int): Int {
        return Color.parseColor(ChartColors.COLORS[id % ChartColors.COLORS.size])
    }

    fun getLegend(): List<String>
    fun getLegend(fromDate: String, toDate: String): List<String> {
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

    private fun getChartDataEntries(categoryList: List<Float>): List<ENTRY> {
        val values = ArrayList<ENTRY>()
        categoryList.forEachIndexed { index, value ->
            values.add(getSpecificEntry(index.toFloat(), value / 100))
        }
        return values
    }

    companion object {
        fun createTimelineData(
            ago: String,
            today: String,
            list: List<PriceByCategory>,
        ): List<List<Float>> {

            val categories = list.map { it.category }.toSortedSet()

            val monthsBetween = calculateMonthsBetween(ago, today)
            val array: ArrayList<ArrayList<Float>> = ArrayList(categories.size)
            for (i in 1..categories.size) {
                array.add(ArrayList())
                for (j in 1..monthsBetween.toInt() + 1) {
                    array[i - 1].add(0F)
                }
            }
            list.forEach {
                val indexOfDate = calculateMonthIndex(ago, it.date)
                if (indexOfDate in 0..monthsBetween) {
                    val categoryIndex = categories.indexOf(it.category)
                    val value = getValueByCategoryAndDate(list, it.category, it.date)
                    array[categoryIndex][indexOfDate] = value
                }
            }
            if (array.size == 0) {
                return listOf(listOf(0F))
            }
            return array
        }

        private fun calculateMonthsBetween(date1Str: String, date2Str: String): Long {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1 = LocalDate.parse(date1Str.substring(0, 7) + "-01", formatter)
            val date2 = LocalDate.parse(date2Str.substring(0, 7) + "-01", formatter)

            return ChronoUnit.MONTHS.between(date1, date2)
        }

        private fun calculateMonthIndex(startDate: String, currentDate: String): Int {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(startDate.substring(0, 7) + "-01", formatter)
            val current = LocalDate.parse(currentDate.substring(0, 7) + "-01", formatter)
            return ChronoUnit.MONTHS.between(start, current).toInt()
        }

        private fun getValueByCategoryAndDate(
            priceByCategoryList: List<PriceByCategory>,
            category: String,
            date: String,
        ): Float {
            return priceByCategoryList.firstOrNull { it.category == category && it.date == date }?.price?.toFloat()
                ?: 0.0f
        }
    }

}