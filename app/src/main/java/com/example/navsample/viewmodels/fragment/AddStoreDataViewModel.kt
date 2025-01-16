package com.example.navsample.viewmodels.fragment

import android.app.Application
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.R
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.NipValidator
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.inputs.StoreErrorInputsMessage
import com.example.navsample.entities.inputs.StoreInputs
import kotlinx.coroutines.launch

class AddStoreDataViewModel(
    private var application: Application
) : AndroidViewModel(application) {

    private var roomDatabaseHelper: RoomDatabaseHelper


    var inputType = AddingInputType.EMPTY.name
    var storeId = ""
    var categoryId = ""
    var storeName: String? = null
    var storeNip: String? = null

    var mode = DataMode.NEW
    var pickedCategory: Category? = null
    var storeInputs: Store = Store()

    var storeList = MutableLiveData<ArrayList<Store>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var storeById = MutableLiveData<Store?>()
    var savedStore = MutableLiveData<Store>()

    init {
        val dao = ReceiptDatabase.getInstance(application).receiptDao
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    fun refreshStoreList() {
        viewModelScope.launch {
            storeList.postValue(roomDatabaseHelper.getAllStores() as ArrayList<Store>)
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            val deletedProducts = roomDatabaseHelper.deleteStoreProducts(storeId)
            FirestoreHelperSingleton.getInstance().delete(deletedProducts) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
            val deletedReceipts = roomDatabaseHelper.deleteStoreReceipts(storeId)
            FirestoreHelperSingleton.getInstance().delete(deletedReceipts) { id ->
                viewModelScope.launch { roomDatabaseHelper.markReceiptAsDeleted(id) }
            }
            val deletedStore = roomDatabaseHelper.deleteStore(storeId)
            FirestoreHelperSingleton.getInstance().delete(deletedStore) { id ->
                viewModelScope.launch { roomDatabaseHelper.markStoreAsDeleted(id) }
            }
        }
    }

    fun getStoreById(id: String) {
        viewModelScope.launch {
            storeById.postValue(roomDatabaseHelper.getStoreById(id))
        }
    }

    fun insertStore(newStore: Store, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedStore = roomDatabaseHelper.insertStore(newStore, generateId)
            savedStore.postValue(insertedStore)
            FirestoreHelperSingleton.getInstance().addFirestore(insertedStore) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateStoreFirestoreId(insertedStore.id, it)
                }
            }
        }
    }

    fun updateStore(newStore: Store) {
        viewModelScope.launch {
            val updateStore = roomDatabaseHelper.updateStore(newStore)
            savedStore.postValue(updateStore)
            if (newStore.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updateStore) {
                    viewModelScope.launch { roomDatabaseHelper.markStoreAsUpdated(newStore.id) }
                }
            }
        }
    }

    fun validateNip(text: CharSequence?): String? {
        val error = isNIPUnique(text)
        if (error != null) {
            return error
        }
        if (!NipValidator.validate(text)) {
            return getString(application, R.string.nip_incorrect)
        }
        return null
    }

    private fun isNIPUnique(text: CharSequence?): String? {
        if (storeById.value?.nip == text) {
            return null
        }
        val index = storeList.value?.map { it.nip }?.indexOf(text) ?: -1
        if (storeList.value?.find { it.nip == text } != null) {
            return getNipExistText() + " " + storeList.value?.get(index)?.name
        }
        return null

    }

    fun validateObligatoryFields(storeInputs: StoreInputs): StoreErrorInputsMessage {
        val errors = StoreErrorInputsMessage()
        if (storeInputs.name.isNullOrEmpty()) {
            errors.name = getEmptyValueText()
        }
        if (storeInputs.categoryId == null) {
            errors.categoryId = getEmptyValueText()
        }
        errors.nip = validateNip(storeInputs.nip)
        errors.nip = isNIPUnique(storeInputs.nip)

        return errors
    }

    private fun getEmptyValueText(): String {
        return getString(application, R.string.empty_value_error)
    }

    private fun getNipExistText(): String {
        return getString(application, R.string.nip_already_exists)
    }
}
