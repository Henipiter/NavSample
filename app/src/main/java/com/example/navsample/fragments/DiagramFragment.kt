package com.example.navsample.fragments


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.navsample.chart.ChartData
import com.example.navsample.chart.ChartMode
import com.example.navsample.chart.MyXAxisFormatter
import com.example.navsample.chart.creator.BarChartCreator
import com.example.navsample.chart.creator.LineChartCreator
import com.example.navsample.chart.creator.PieChartCreator
import com.example.navsample.chart.creator.RadarChartCreator
import com.example.navsample.databinding.FragmentDiagramBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.github.mikephil.charting.components.Legend
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class DiagramFragment : Fragment() {
    private var _binding: FragmentDiagramBinding? = null
    private val binding get() = _binding!!

    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private var mode = ChartMode.PIE
    private var categoryData: MutableList<ChartData> = mutableListOf()
    private var timelineData: MutableList<Pair<String, ChartData>> = mutableListOf()

    private lateinit var today: String
    private lateinit var ago: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDiagramBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        updateData(binding.monthRangeSlider.value.toInt())



        binding.monthRangeSlider.addOnChangeListener { slider, value, fromUser ->
            updateData(value.toInt())
            // Responds to when slider's value is changed
        }


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> {
                            mode = ChartMode.PIE
                            binding.lineChart.visibility = View.INVISIBLE
                            binding.radarChart.visibility = View.INVISIBLE
                            binding.barChart.visibility = View.INVISIBLE
                            binding.pieChart.visibility = View.VISIBLE
                        }

                        1 -> {
                            mode = ChartMode.RADAR
                            binding.lineChart.visibility = View.INVISIBLE
                            binding.radarChart.visibility = View.VISIBLE
                            binding.barChart.visibility = View.INVISIBLE
                            binding.pieChart.visibility = View.INVISIBLE
                        }

                        2 -> {
                            mode = ChartMode.BAR
                            binding.lineChart.visibility = View.INVISIBLE
                            binding.radarChart.visibility = View.INVISIBLE
                            binding.barChart.visibility = View.VISIBLE
                            binding.pieChart.visibility = View.INVISIBLE
                        }

                        3 -> {
                            mode = ChartMode.LINE
                            binding.lineChart.visibility = View.VISIBLE
                            binding.radarChart.visibility = View.INVISIBLE
                            binding.barChart.visibility = View.INVISIBLE
                            binding.pieChart.visibility = View.INVISIBLE
                        }
                    }
                    updateData(binding.monthRangeSlider.value.toInt())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })



        binding.pieChart.setUsePercentValues(false)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setExtraOffsets(5F, 10F, 5F, 5F)


        binding.pieChart.transparentCircleRadius = 21f
        binding.pieChart.holeRadius = 16f

        val l: Legend = binding.pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.isWordWrapEnabled = true
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = Color.WHITE
        l.form = Legend.LegendForm.CIRCLE

        binding.pieChart.setEntryLabelColor(Color.WHITE)
        binding.pieChart.setEntryLabelTextSize(12f)


        binding.lineChart.resetTracking()
        val l1 = binding.lineChart.legend
        l1.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l1.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l1.orientation = Legend.LegendOrientation.HORIZONTAL
        l1.textColor = Color.WHITE
        l1.setDrawInside(false)

    }

    private fun updateData(monthsAgo: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val threeMonthsAgo =
            Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }.time

        today = dateFormat.format(Date())
        ago = dateFormat.format(threeMonthsAgo)


        when (mode) {
            ChartMode.PIE -> receiptDataViewModel.getChartDataCategory(ago, today)
            ChartMode.RADAR -> receiptDataViewModel.getChartDataCategory(ago, today)
            ChartMode.BAR -> receiptDataViewModel.getChartDataTimeline(ago, today)
            ChartMode.LINE -> receiptDataViewModel.getChartDataTimeline(ago, today)

        }
    }


    private fun initObserver() {
        receiptDataViewModel.chartData.observe(viewLifecycleOwner) { it ->
            it?.let {

                when (mode) {
                    ChartMode.PIE -> {
                        val pieChartCreator = PieChartCreator()
                        val data = pieChartCreator.getData(it)

                        binding.pieChart.data = data
                        binding.pieChart.highlightValues(null)
                        binding.pieChart.invalidate()
                    }

                    ChartMode.RADAR -> {
                        val radarChartCreator = RadarChartCreator()
                        val data = radarChartCreator.getData(it)

                        binding.radarChart.data = data
                        binding.radarChart.highlightValues(null)
                        binding.radarChart.invalidate()
                    }

                    ChartMode.BAR -> {

                        val barChartCreator = BarChartCreator(ago, today, it)
                        val data = barChartCreator.getData(it)
                        val legendDates = barChartCreator.getDateLegend(ago, today)


                        val groupSpace = 0.1f
                        val barSpace = 0.01f
                        val barWidth = 0.23f

                        data.setValueTextColor(Color.WHITE)
                        binding.barChart.setDrawValueAboveBar(true)
                        binding.barChart.data = data

                        binding.barChart.barData.barWidth = barWidth
                        binding.barChart.xAxis.valueFormatter = MyXAxisFormatter(legendDates)
                        binding.barChart.xAxis.setCenterAxisLabels(true)
                        binding.barChart.setVisibleXRangeMaximum(4F)
                        binding.barChart.moveViewToX(1F)

                        binding.barChart.groupBars(1F, groupSpace, barSpace)
                        binding.barChart.invalidate()
                    }

                    ChartMode.LINE -> {

                        val lineChartCreator = LineChartCreator(ago, today, it)
                        val data = lineChartCreator.getData(it)
                        val legendDates = lineChartCreator.getDateLegend(ago, today)

                        binding.lineChart.data = data
                        binding.lineChart.invalidate()
                        binding.lineChart.description.isEnabled = false
                        binding.lineChart.setNoDataTextColor(Color.WHITE)
                        binding.lineChart.xAxis.valueFormatter = MyXAxisFormatter(legendDates)

                        binding.lineChart.xAxis.textColor = Color.WHITE
                        binding.lineChart.axisLeft.textColor = Color.WHITE
                        binding.lineChart.axisRight.textColor = Color.WHITE
                        binding.lineChart.description.textColor = Color.WHITE
                        binding.lineChart.xAxis.enableGridDashedLine(10f, 20f, 10f)
                        binding.lineChart.axisLeft.enableGridDashedLine(10f, 20f, 10f)
                        binding.lineChart.axisRight.enableGridDashedLine(10f, 20f, 10f)
                    }

                }
            }
        }
    }
}