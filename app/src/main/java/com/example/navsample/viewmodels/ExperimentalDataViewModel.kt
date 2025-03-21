package com.example.navsample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import kotlinx.coroutines.launch

class ExperimentalDataViewModel(
    application: Application
) : AndroidViewModel(application) {
    var categoryList = MutableLiveData<ArrayList<Category>>()

    lateinit var userOrderedName: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var userOrderedPrices: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var algorithmOrderedNames: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>
    lateinit var algorithmOrderedPrices: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>

    private val dao = ReceiptDatabase.getInstance(application).receiptDao
    private var roomDatabaseHelper = RoomDatabaseHelper(dao)

    init {
        clearData()
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    private fun clearData() {
        userOrderedName = MutableLiveData(arrayListOf())
        userOrderedPrices = MutableLiveData(arrayListOf())
        algorithmOrderedNames = MutableLiveData(
            arrayListOf(
                AlgorithmItemAdapterArgument("Name100"),
                AlgorithmItemAdapterArgument("Name01"),
                AlgorithmItemAdapterArgument("Name02"),
                AlgorithmItemAdapterArgument("Name03"),
                AlgorithmItemAdapterArgument("Name04"),
                AlgorithmItemAdapterArgument("Name05"),
                AlgorithmItemAdapterArgument("Name06"),
                AlgorithmItemAdapterArgument("Name07"),
                AlgorithmItemAdapterArgument("Name08"),
                AlgorithmItemAdapterArgument("Name09"),
                AlgorithmItemAdapterArgument("Name10"),
                AlgorithmItemAdapterArgument("Name11"),
                AlgorithmItemAdapterArgument("Name12"),
                AlgorithmItemAdapterArgument("Name13"),
                AlgorithmItemAdapterArgument("Name14"),
                AlgorithmItemAdapterArgument("Name15"),
                AlgorithmItemAdapterArgument("Name16"),
                AlgorithmItemAdapterArgument("Name17"),
                AlgorithmItemAdapterArgument("Name18"),
                AlgorithmItemAdapterArgument("Name19")
            )
        )
        algorithmOrderedPrices = MutableLiveData(
            arrayListOf(
                AlgorithmItemAdapterArgument("Price00"),
                AlgorithmItemAdapterArgument("Price01"),
                AlgorithmItemAdapterArgument("Price02"),
                AlgorithmItemAdapterArgument("Price03"),
                AlgorithmItemAdapterArgument("Price04"),
                AlgorithmItemAdapterArgument("Price05"),
                AlgorithmItemAdapterArgument("Price06"),
                AlgorithmItemAdapterArgument("Price07"),
                AlgorithmItemAdapterArgument("Price08"),
                AlgorithmItemAdapterArgument("Price09"),
                AlgorithmItemAdapterArgument("Price10"),
                AlgorithmItemAdapterArgument("Price11"),
                AlgorithmItemAdapterArgument("Price12"),
                AlgorithmItemAdapterArgument("Price13"),
                AlgorithmItemAdapterArgument("Price14"),
                AlgorithmItemAdapterArgument("Price15"),
                AlgorithmItemAdapterArgument("Price16"),
                AlgorithmItemAdapterArgument("Price17"),
                AlgorithmItemAdapterArgument("Price18"),
                AlgorithmItemAdapterArgument("Price19"),
            )
        )
    }
}
