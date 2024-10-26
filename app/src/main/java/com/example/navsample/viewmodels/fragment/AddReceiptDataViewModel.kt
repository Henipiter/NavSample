package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch
import java.util.UUID

class AddReceiptDataViewModel : ViewModel() {

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper

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
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
        }
    }

    fun getReceiptById(id: String) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                receiptById.postValue(dao.getReceiptById(id))
            }
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        Log.i(
            "Database",
            "update receipt with id ${newReceipt.id}: ${newReceipt.date} ${newReceipt.pln}"
        )
        viewModelScope.launch {
            dao?.let {
                dao.updateReceipt(newReceipt)
            }
            savedReceipt.postValue(newReceipt)
            firebaseHelper.updateFirestore(newReceipt)
        }
    }


    fun deleteReceipt(receiptId: String) {
        Log.i("Database", "delete receipt - id $receiptId")
        viewModelScope.launch {
            dao?.deleteProductsOfReceipt(receiptId)
            dao?.deleteReceiptById(receiptId)
        }
    }

    fun insertReceipt(newReceipt: Receipt) {
        Log.i("Database", "insert receipt: ${newReceipt.date} ${newReceipt.pln}")
        viewModelScope.launch {
            dao?.let {
                newReceipt.date = convertDateFormat(newReceipt.date)
                newReceipt.id = UUID.randomUUID().toString()
                dao.insertReceipt(newReceipt)
            }
            Log.i("Database", "inserted receipt with id ${newReceipt.id}")
            savedReceipt.value = newReceipt
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


    private fun convertDateFormat(date: String): String {
        val newDate = date.replace(".", "-")
        val splitDate = newDate.split("-")
        try {
            if (splitDate[2].length == 4) {
                return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0]
            }
            return newDate
        } catch (e: Exception) {
            Log.e("ConvertDate", "Cannot convert date: $splitDate")
            return newDate
        }
    }

}
