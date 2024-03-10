package com.example.navsample.chart.creator


import android.graphics.Color
import com.example.navsample.chart.MyXAxisFormatter
import com.example.navsample.entities.relations.PriceByCategory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class LineChartCreator : LinearChartFactory<Entry, LineDataSet, ILineDataSet, LineData, LineChart> {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time
    private var today = dateFormat.format(Date())
    private var ago = dateFormat.format(threeMonthsAgo)

    override fun getTitle(): String {
        return "Line Chart"
    }

    override fun getData(data: List<PriceByCategory>): LineData {
        val categories = data.map { it.category }.toSortedSet()
        if (categories.size == 0) {
            categories.add("")
        }
        val timelineData = LinearChartFactory.createTimelineData(ago, today, data)
        val dataSets = convertToChart(categories, timelineData)
        return getSpecificChartData(dataSets)
    }

    override fun getSpecificDataSet(entries: List<Entry>): LineDataSet {
        return LineDataSet(entries, getTitle())
    }

    override fun setDateRange(ago: String, currentDate: String) {
        this.ago = ago
        this.today = currentDate
    }

    override fun getSpecificEntry(x: Float, y: Float): Entry {
        return Entry(x, y)
    }

    override fun getSpecificChartData(dataset: List<ILineDataSet>): LineData {
        return LineData(dataset)
    }

    override fun createDataSet(
        values: List<Entry>, categories: Set<String>, id: Int
    ): ILineDataSet {

        val dataSet = LineDataSet(values, categories.toList()[id])
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 4f
        dataSet.valueTextColor = Color.WHITE

        val color = getChartColor(id)
        dataSet.color = color
        dataSet.setCircleColor(color)
        return dataSet
    }

    override fun initializeChart(chart: LineChart) {
        chart.resetTracking()

        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.textColor = Color.WHITE
        chart.legend.setDrawInside(false)
    }

    override fun drawChart(chart: LineChart, data: LineData) {
        chart.data = data
        chart.description.isEnabled = false
        chart.setNoDataTextColor(Color.WHITE)
        chart.xAxis.valueFormatter = MyXAxisFormatter(getDateLegend(ago, today))

        chart.xAxis.textColor = Color.WHITE
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.textColor = Color.WHITE
        chart.description.textColor = Color.WHITE
        chart.xAxis.enableGridDashedLine(10f, 20f, 10f)
        chart.axisLeft.enableGridDashedLine(10f, 20f, 10f)
        chart.axisRight.enableGridDashedLine(10f, 20f, 10f)

        chart.invalidate()
    }

}
