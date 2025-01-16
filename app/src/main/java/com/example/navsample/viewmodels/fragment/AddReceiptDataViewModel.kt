package com.example.navsample.viewmodels.fragment

import android.app.Application
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.R
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.PriceUtils.Companion.doublePriceTextToInt
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.inputs.ReceiptErrorInputsMessage
import com.example.navsample.entities.inputs.ReceiptInputs
import kotlinx.coroutines.launch

class AddReceiptDataViewModel(
    private var application: Application
) : AndroidViewModel(application) {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var receiptId = ""
    var storeId = ""

    var pickedStore: Store? = null
    var receiptInputs: Receipt = Receipt()
    var mode = DataMode.NEW

    var storeList = MutableLiveData<ArrayList<Store>>()
    var receiptById = MutableLiveData<Receipt?>()
    var savedReceipt = MutableLiveData<Receipt>()

    init {
        val dao = ReceiptDatabase.getInstance(application).receiptDao
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

    fun save(receiptInputs: ReceiptInputs, afterInsert: () -> Unit, afterUpdate: () -> Unit) {
        if (mode == DataMode.NEW) {
            if (pickedStore != null && pickedStore?.id?.isEmpty() == false) {
                val receipt = Receipt(
                    receiptInputs.storeId!!,
                    doublePriceTextToInt(receiptInputs.pln),
                    doublePriceTextToInt(receiptInputs.ptu),
                    receiptInputs.date.toString(),
                    receiptInputs.time.toString()
                )
                insertReceipt(receipt)
                afterInsert.invoke()
            }
        } else if (mode == DataMode.EDIT) {
            receiptById.value?.let {
                if (pickedStore != null && pickedStore?.id?.isEmpty() == false) {
                    val receipt = Receipt(
                        receiptInputs.storeId!!,
                        doublePriceTextToInt(receiptInputs.pln),
                        doublePriceTextToInt(receiptInputs.ptu),
                        receiptInputs.date.toString(),
                        receiptInputs.time.toString()
                    )
                    receipt.id = it.id
                    updateReceipt(receipt)
                    afterUpdate.invoke()
                }
            }
        }
    }

    fun validateObligatoryFields(receiptInputs: ReceiptInputs): ReceiptErrorInputsMessage {
        val errors = ReceiptErrorInputsMessage()
        if (receiptInputs.storeId.isNullOrEmpty()) {
            errors.storeId = getString(application, R.string.pick_store)
        }
        if (receiptInputs.pln.isNullOrEmpty()) {
            errors.pln = getString(application, R.string.empty_value_error)
        }
        if (receiptInputs.ptu.isNullOrEmpty()) {
            errors.ptu = getString(application, R.string.empty_value_error)
        }
        if (receiptInputs.date.isNullOrEmpty()) {
            errors.date = getString(application, R.string.empty_value_error)
        }
        if (receiptInputs.time.isNullOrEmpty()) {
            errors.time = getString(application, R.string.empty_value_error)
        }
        return errors
    }
}
