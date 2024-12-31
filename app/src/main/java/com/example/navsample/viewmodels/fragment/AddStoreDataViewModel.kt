package com.example.navsample.viewmodels.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Store
import kotlinx.coroutines.launch

class AddStoreDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper


    var inputType = AddingInputType.EMPTY.name
    var storeId = ""
    var categoryId = ""
    var storeName: String? = null
    var storeNip: String? = null

    var storeList = MutableLiveData<ArrayList<Store>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var storeById = MutableLiveData<Store?>()
    var savedStore = MutableLiveData<Store>()

    init {
        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
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


}
