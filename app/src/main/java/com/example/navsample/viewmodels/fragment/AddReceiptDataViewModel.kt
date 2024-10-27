package com.example.navsample.viewmodels.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch

class AddReceiptDataViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var receiptId = ""
    var storeId = ""

    var storeList = MutableLiveData<ArrayList<Store>>()
    var receiptById = MutableLiveData<Receipt?>()
    var savedReceipt = MutableLiveData<Receipt>()

    init {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        val userUuid = myPref?.getString("userId", null) ?: throw Exception("NOT SET userId")
        firebaseHelper = FirebaseHelper(userUuid)

        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshStoreList() {
        viewModelScope.launch {
            storeList.postValue(roomDatabaseHelper.getAllStores() as ArrayList)
        }
    }

    fun getReceiptById(id: String) {
        viewModelScope.launch {
            receiptById.postValue(roomDatabaseHelper.getReceiptById(id))
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            val updatedReceipt = roomDatabaseHelper.updateReceipt(newReceipt)
            savedReceipt.postValue(updatedReceipt)
            firebaseHelper.updateFirestore(updatedReceipt)
        }
    }


    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            val deletedProducts = roomDatabaseHelper.deleteReceiptProducts(receiptId)
            firebaseHelper.delete(deletedProducts)
            val deletedReceipt = roomDatabaseHelper.deleteReceipt(receiptId)
            firebaseHelper.delete(deletedReceipt)
        }
    }

    fun insertReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            val insertedReceipt = roomDatabaseHelper.insertReceipt(newReceipt)
            savedReceipt.postValue(insertedReceipt)
            firebaseHelper.addFirestore(insertedReceipt) {
                insertedReceipt.firestoreId = it
                updateReceipt(newReceipt)
            }
        }
    }

}
