package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.TranslateEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AddReceiptDataViewModel : ViewModel() {

    companion object {
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
    }

    private val firestore = Firebase.firestore
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }


    var inputType = "EMPTY"
    var receiptId = -1

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

    fun getReceiptById(id: Int) {
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
            updateFirestore(newReceipt)
        }
    }


    fun deleteReceipt(receiptId: Int) {
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
                val rowId = dao.insertReceipt(newReceipt)
                newReceipt.id = dao.getReceiptId(rowId)
            }
            Log.i("Database", "inserted receipt with id ${newReceipt.id}")
            savedReceipt.value = newReceipt
            addFirestore(newReceipt)
        }
    }


    private fun <T : TranslateEntity> addFirestore(obj: T) {
        getFirestoreUserPath().document(obj.getDescriptiveId()).set(obj)
    }

    private fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath()
            .document(obj.getDescriptiveId())
            .update(obj.toMap())

    }

    private fun getFirestoreUserPath(): CollectionReference {
        return firestore.collection("user").document(userUuid.value.toString())
            .collection(RECEIPT_FIRESTORE_PATH)

    }

    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
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
