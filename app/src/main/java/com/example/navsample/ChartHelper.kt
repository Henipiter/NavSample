package com.example.navsample

import android.graphics.Color
import com.example.navsample.DTO.ChartColors
import com.example.navsample.DTO.ChartData
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.SortedSet

class ChartHelper {

    fun createTimelineData(
        ago: String,
        today: String,
        list: ArrayList<PriceByCategory>,
    ): ArrayList<ArrayList<Float>> {

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
            return arrayListOf(arrayListOf(0F))
        }
        return array
    }

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

    fun convertToLineChart(
        categories: SortedSet<String>,
        array: ArrayList<ArrayList<Float>>,
    ): ArrayList<ILineDataSet> {
        val dataSets = ArrayList<ILineDataSet>()
        array.forEachIndexed { id, categoryList ->


            val values = ArrayList<Entry>()
            categoryList.forEachIndexed { index, value ->
                values.add(Entry(index.toFloat(), value))
            }


            val d = LineDataSet(values, categories.toList()[id])
            d.lineWidth = 2.5f
            d.circleRadius = 4f
            d.valueTextColor = Color.WHITE

            val color = Color.parseColor(ChartColors.COLORS[id % ChartColors.COLORS.size])
            d.color = color
            d.setCircleColor(color)
            dataSets.add(d)
        }
        return dataSets
    }

    fun convertToBarChart(
        categories: SortedSet<String>,
        array: ArrayList<ArrayList<Float>>,
    ): ArrayList<IBarDataSet> {
        val dataSets = ArrayList<IBarDataSet>()
        array.forEachIndexed { id, categoryList ->


            val values = ArrayList<BarEntry>()
            categoryList.forEachIndexed { index, value ->
                values.add(BarEntry(index.toFloat(), value))
            }


            val d = BarDataSet(values, categories.toList()[id])

            d.valueTextColor = Color.WHITE

            val color = ChartColors.COLORS[id % ChartColors.COLORS.size]
            d.color = Color.parseColor(color)
            dataSets.add(d)
        }
        return dataSets
    }

    fun getPieData(title: String, chartData: List<ChartData>): PieDataSet {

        val entries = ArrayList<PieEntry>()
        chartData.forEach {
            entries.add(PieEntry(it.value, it.label))
        }

        val dataSet = PieDataSet(entries, title)
        dataSet.setDrawIcons(false)

        dataSet.colors = getColors()
        return dataSet
    }

    fun getRadarData(title: String, chartData: List<ChartData>): RadarDataSet {

        val entries = ArrayList<RadarEntry>()
        chartData.forEach {
            entries.add(RadarEntry(it.value, it.label))
        }

        val dataSet = RadarDataSet(entries, title)
        dataSet.setDrawIcons(false)

        dataSet.colors = getColors()
        return dataSet
    }

    private fun calculateDaysBetween(date1Str: String, date2Str: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date1 = LocalDate.parse(date1Str, formatter)
        val date2 = LocalDate.parse(date2Str, formatter)

        return ChronoUnit.DAYS.between(date1, date2)
    }

    private fun calculateMonthsBetween(date1Str: String, date2Str: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date1 = LocalDate.parse(date1Str.substring(0, 7) + "-01", formatter)
        val date2 = LocalDate.parse(date2Str.substring(0, 7) + "-01", formatter)

        return ChronoUnit.MONTHS.between(date1, date2)
    }

    private fun getColors(): ArrayList<Int> {
        val colors = ArrayList<Int>()
        for (c in ChartColors.COLORS) colors.add(Color.parseColor(c))
        colors.add(ColorTemplate.getHoloBlue())
        return colors
    }

    private fun calculateDayIndex(startDate: String, currentDate: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(startDate, formatter)
        val currentDate = LocalDate.parse(currentDate, formatter)
        return ChronoUnit.DAYS.between(startDate, currentDate).toInt()
    }

    private fun calculateMonthIndex(startDate: String, currentDate: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(startDate.substring(0, 7) + "-01", formatter)
        val currentDate = LocalDate.parse(currentDate.substring(0, 7) + "-01", formatter)
        return ChronoUnit.MONTHS.between(startDate, currentDate).toInt()
    }

    private fun getValueByCategoryAndDate(
        priceByCategoryList: ArrayList<PriceByCategory>,
        category: String,
        date: String,
    ): Float {
        return priceByCategoryList.filter { it.category == category && it.date == date }
            .firstOrNull()?.price ?: 0f
    }

}