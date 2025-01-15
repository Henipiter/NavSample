package com.example.navsample.viewmodels.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import kotlinx.coroutines.launch

class AddReceiptDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var receiptId = ""
    var storeId = ""
    var pickedStore: Store? = null
    var receiptInputs: Receipt = Receipt()

    var storeList = MutableLiveData<ArrayList<Store>>()
    var receiptById = MutableLiveData<Receipt?>()
    var savedReceipt = MutableLiveData<Receipt>()

    init {
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


    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            val deletedProducts = roomDatabaseHelper.deleteReceiptProducts(receiptId)
            FirestoreHelperSingleton.getInstance().delete(deletedProducts) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
            val deletedReceipt = roomDatabaseHelper.deleteReceipt(receiptId)
            FirestoreHelperSingleton.getInstance().delete(deletedReceipt) { id ->
                viewModelScope.launch { roomDatabaseHelper.markReceiptAsDeleted(id) }
            }
        }
    }

    fun insertReceipt(newReceipt: Receipt, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedReceipt = roomDatabaseHelper.insertReceipt(newReceipt, generateId)
            savedReceipt.postValue(insertedReceipt)
            FirestoreHelperSingleton.getInstance().addFirestore(insertedReceipt) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateReceiptFirestoreId(insertedReceipt.id, it)
                }
            }
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            val updatedReceipt = roomDatabaseHelper.updateReceipt(newReceipt)
            savedReceipt.postValue(updatedReceipt)
            if (newReceipt.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updatedReceipt) { id ->
                    viewModelScope.launch { roomDatabaseHelper.markReceiptAsUpdated(id) }
                }
            }
        }
    }

}
