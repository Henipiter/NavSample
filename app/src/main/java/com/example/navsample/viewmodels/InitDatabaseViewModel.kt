package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store

class InitDatabaseViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var imageUuid = MutableLiveData<String>()
    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>

    lateinit var category: MutableLiveData<Category?>

    init {
        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
        roomDatabaseHelper = RoomDatabaseHelper(dao!!)
    }

}
