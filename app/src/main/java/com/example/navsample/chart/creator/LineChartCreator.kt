package com.example.navsample.chart.creator


import android.graphics.Color
import com.example.navsample.chart.MyXAxisFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class LineChartCreator : LinearChartFactory<Entry, ILineDataSet, LineData, LineChart> {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time
    private var today = dateFormat.format(Date())
    private var ago = dateFormat.format(threeMonthsAgo)
    override fun setDateRange(ago: String, currentDate: String) {
        this.ago = ago
        this.today = currentDate
    }

    override fun initializeChart(chart: LineChart) {
        chart.resetTracking()

        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.textColor = Color.WHITE
        chart.legend.setDrawInside(false)
    }

    override fun drawChart(chart: LineChart, data: LineData, legend: List<String>) {
        chart.data = data
        chart.description.isEnabled = false
        chart.setNoDataTextColor(Color.WHITE)
        chart.xAxis.valueFormatter = MyXAxisFormatter(legend)

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
