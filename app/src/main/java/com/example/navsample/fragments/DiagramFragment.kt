package com.example.navsample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.navsample.chart.ChartMode
import com.example.navsample.chart.creator.BarChartCreator
import com.example.navsample.chart.creator.LineChartCreator
import com.example.navsample.chart.creator.PieChartCreator
import com.example.navsample.chart.creator.RadarChartCreator
import com.example.navsample.chart.data.BarDataCreator
import com.example.navsample.chart.data.LineDataCreator
import com.example.navsample.chart.data.PieDataCreator
import com.example.navsample.chart.data.RadarDataCreator
import com.example.navsample.databinding.FragmentDiagramBinding
import com.example.navsample.viewmodels.ChartDataViewModel
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class DiagramFragment : Fragment() {
    private var _binding: FragmentDiagramBinding? = null
    private val binding get() = _binding!!

    private val chartDataViewModel: ChartDataViewModel by activityViewModels()

    private var mode = ChartMode.PIE

    private lateinit var today: String
    private lateinit var ago: String

    private var pieChartCreator = PieChartCreator()
    private var radarChartCreator = RadarChartCreator()
    private var barChartCreator = BarChartCreator()
    private var lineChartCreator = LineChartCreator()

    private var pieDataCreator = PieDataCreator()
    private var radarDataCreator = RadarDataCreator()
    private var barDataCreator = BarDataCreator()
    private var lineDataCreator = LineDataCreator()

    private var sumLastMonths = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDiagramBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialCharts()
        initObserver()
        setChartVisibility(0)
        updateData(binding.monthRangeSlider.value.toInt())
        binding.monthRangeSlider.addOnChangeListener { _, value, _ ->
            updateData(value.toInt())
        }
        binding.radioGroup.setOnCheckedChangeListener { group, _ ->
            sumLastMonths = group.checkedRadioButtonId == binding.sumMonthButton.id
            updateData(binding.monthRangeSlider.value.toInt())
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    setChartVisibility(it.position)
                    refreshChart()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun initialCharts() {
        pieChartCreator.initializeChart(binding.pieChart)
        radarChartCreator.initializeChart(binding.radarChart)
        lineChartCreator.initializeChart(binding.lineChart)
        barChartCreator.initializeChart(binding.barChart)
    }

    private fun updateData(monthsAgo: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val threeMonthsAgo = calendar.apply { add(Calendar.MONTH, -monthsAgo) }.time

        today = if (sumLastMonths) {
            dateFormat.format(Date())
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            dateFormat.format(calendar.time)
        }
        ago = dateFormat.format(threeMonthsAgo)
        val rangeText = "$ago - $today"
        binding.rangeText.text = rangeText
        updateRangeOfTimelineChart(today, ago)
        chartDataViewModel.getChartDataCategory(ago, today)
        chartDataViewModel.getChartDataTimeline(ago, today)

    }

    private fun updateRangeOfTimelineChart(today: String, ago: String) {
        barDataCreator.setDateRange(ago, today)
        lineDataCreator.setDateRange(ago, today)
    }


    private fun initObserver() {
        chartDataViewModel.categoryChartData.observe(viewLifecycleOwner) {
            if (mode == ChartMode.PIE || mode == ChartMode.RADAR) {
                refreshChart()
            }
        }
        chartDataViewModel.timelineChartData.observe(viewLifecycleOwner) {
            if (mode == ChartMode.BAR || mode == ChartMode.LINE) {
                refreshChart()
            }
        }
    }

    private fun refreshChart() {
        when (mode) {
            ChartMode.PIE -> {
                chartDataViewModel.categoryChartData.value?.let {
                    val data = pieDataCreator.getData(it)
                    val legend = pieDataCreator.getLegend(it)
                    pieChartCreator.drawChart(binding.pieChart, data, legend)
                }
            }

            ChartMode.RADAR -> {
                chartDataViewModel.categoryChartData.value?.let {
                    val data = radarDataCreator.getData(it)
                    val legend = radarDataCreator.getLegend(it)
                    radarChartCreator.drawChart(binding.radarChart, data, legend)
                }
            }

            ChartMode.BAR -> {
                chartDataViewModel.timelineChartData.value?.let {
                    val data = barDataCreator.getData(it)
                    val legend = barDataCreator.getLegend()
                    barChartCreator.drawChart(binding.barChart, data, legend)
                }
            }

            ChartMode.LINE -> {
                chartDataViewModel.timelineChartData.value?.let {
                    val data = lineDataCreator.getData(it)
                    val legend = lineDataCreator.getLegend()
                    lineChartCreator.drawChart(binding.lineChart, data, legend)
                }
            }

        }
    }

    private fun setChartVisibility(position: Int) {
        when (position) {
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
                mode = ChartMode.LINE
                binding.lineChart.visibility = View.VISIBLE
                binding.radarChart.visibility = View.INVISIBLE
                binding.barChart.visibility = View.INVISIBLE
                binding.pieChart.visibility = View.INVISIBLE
            }

            3 -> {
                mode = ChartMode.BAR
                binding.lineChart.visibility = View.INVISIBLE
                binding.radarChart.visibility = View.INVISIBLE
                binding.barChart.visibility = View.VISIBLE
                binding.pieChart.visibility = View.INVISIBLE

            }
        }
    }
}
