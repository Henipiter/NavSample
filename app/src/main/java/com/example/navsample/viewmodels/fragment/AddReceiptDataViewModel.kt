package com.example.navsample.viewmodels.fragment

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

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)

    var inputType = AddingInputType.EMPTY.name
    var receiptId = ""
    var storeId = ""

    var storeList = MutableLiveData<ArrayList<Store>>()
    var receiptById = MutableLiveData<Receipt?>()
    var savedReceipt = MutableLiveData<Receipt>()
    private var userUuid = MutableLiveData<String?>(null)

    init {
        setUserUuid()
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
            roomDatabaseHelper.updateReceipt(newReceipt)
            savedReceipt.postValue(newReceipt)
            firebaseHelper.updateFirestore(newReceipt)
        }
    }


    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            roomDatabaseHelper.deleteReceipt(receiptId)
        }
    }

    fun insertReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            savedReceipt.postValue(roomDatabaseHelper.insertReceipt(newReceipt))
            firebaseHelper.addFirestore(newReceipt)
        }
    }


    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
                firebaseHelper = FirebaseHelper(uuid!!)
            }
        }
    }


}
