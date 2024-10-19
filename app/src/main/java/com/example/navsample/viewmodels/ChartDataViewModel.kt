package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.relations.PriceByCategory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class ChartDataViewModel : ViewModel() {


    //------------------------------
    lateinit var imageUuid: MutableLiveData<String>


    lateinit var timelineChartData: MutableLiveData<List<PriceByCategory>>
    lateinit var categoryChartData: MutableLiveData<List<PriceByCategory>>


    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private val firestore = Firebase.firestore

    init {
        clearData()
    }


    fun clearData() {

        //---------------


        timelineChartData = MutableLiveData<List<PriceByCategory>>(null)
        categoryChartData = MutableLiveData<List<PriceByCategory>>(null)


    }


    fun getChartDataTimeline(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            timelineChartData.postValue(dao?.getPricesForCategoryComparisonWithDate(
                dateFrom, dateTo
            )?.let { ArrayList(it) })
        }
    }

    fun getChartDataCategory(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            categoryChartData.postValue(dao?.getPricesForCategoryComparison(dateFrom, dateTo)
                ?.let { ArrayList(it) })
        }
    }


}
