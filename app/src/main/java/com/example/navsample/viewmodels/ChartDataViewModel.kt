package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.relations.PriceByCategory
import kotlinx.coroutines.launch

class ChartDataViewModel : ViewModel() {

    var timelineChartData = MutableLiveData<List<PriceByCategory>>()
    var categoryChartData = MutableLiveData<List<PriceByCategory>>()


    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)

    fun getChartDataTimeline(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            timelineChartData.postValue(
                roomDatabaseHelper.getPricesForCategoryComparisonWithDate(dateFrom, dateTo)
                        as ArrayList<PriceByCategory>
            )
        }
    }

    fun getChartDataCategory(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            categoryChartData.postValue(
                roomDatabaseHelper.getPricesForCategoryComparison(dateFrom, dateTo)
                        as ArrayList<PriceByCategory>
            )
        }
    }
}
