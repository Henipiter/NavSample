package com.example.navsample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.TableCounts
import kotlinx.coroutines.launch

class ExportDataViewModel(
    application: Application
) : AndroidViewModel(application) {

    var allData = MutableLiveData<ArrayList<AllData>>()
    var tableCounts = MutableLiveData<ArrayList<TableCounts>>()

    private val dao = ReceiptDatabase.getInstance(application).receiptDao
    private var roomDatabaseHelper = RoomDatabaseHelper(dao)
    fun getTableCounts() {
        viewModelScope.launch {
            tableCounts.postValue(roomDatabaseHelper.getTableCounts() as ArrayList<TableCounts>)
        }
    }

    fun getAllData() {
        viewModelScope.launch {
            allData.postValue(roomDatabaseHelper.getAllData() as ArrayList<AllData>)
        }
    }
}
