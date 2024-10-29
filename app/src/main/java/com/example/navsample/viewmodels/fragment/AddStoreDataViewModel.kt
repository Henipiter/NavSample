package com.example.navsample.viewmodels.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.DateUtil
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddStoreDataViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
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
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        val userUuid = myPref?.getString("userId", null) ?: throw Exception("NOT SET userId")
        firebaseHelper = FirebaseHelper(userUuid)

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

    fun updateStoreIfNeeded(store: Store) {
        if (store.firestoreId == "") {
            return
        }
        viewModelScope.launch {
            val localStoreData = roomDatabaseHelper.getStoreForFirestore(store.firestoreId)
            if (localStoreData != null) {
                if (store.isSync && store.updatedAt > localStoreData.updatedAt) {
                    roomDatabaseHelper.updateStore(store, false)
                }
            } else {
                if (store.isSync) {
                    roomDatabaseHelper.insertStore(store, false)
                }
            }
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            val deletedProducts = roomDatabaseHelper.deleteStoreProducts(storeId)
            firebaseHelper.delete(deletedProducts)
            val deletedReceipts = roomDatabaseHelper.deleteStoreReceipts(storeId)
            firebaseHelper.delete(deletedReceipts)
            val deletedStore = roomDatabaseHelper.deleteStore(storeId)
            firebaseHelper.delete(deletedStore)
        }
    }

    fun getStoreById(id: String) {
        viewModelScope.launch {
            roomDatabaseHelper.getStoreById(id)
        }
    }

    fun insertStore(newStore: Store, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedStore = roomDatabaseHelper.insertStore(newStore, generateId)
            savedStore.postValue(insertedStore)
            firebaseHelper.addFirestore(insertedStore) {
                viewModelScope.launch {
                    updateStoreIdInReceipt(insertedStore.id, it)
                    withContext(Dispatchers.IO) {
                        val isCategorySynced =
                            roomDatabaseHelper.isCategorySynced(insertedStore.defaultCategoryId)
                        if (!insertedStore.isSync && isCategorySynced) {
                            insertedStore.isSync = true
                            insertedStore.updatedAt = DateUtil.getCurrentUtcTime()
                        }
                        insertedStore.id = it
                        insertedStore.firestoreId = it
                        updateStoreId(insertedStore)
                        firebaseHelper.updateFirestore(insertedStore)
                    }

                }
            }
        }
    }

    private suspend fun updateStoreIdInReceipt(oldCategoryId: String, newCategoryId: String) {
        roomDatabaseHelper.updateStoreIdInReceipt(oldCategoryId, newCategoryId) {
            firebaseHelper.updateFirestore(it)
        }
    }

    private fun updateStoreId(store: Store) {
        viewModelScope.launch {
            roomDatabaseHelper.updateStoreId(store)
        }
    }

    fun updateStore(newStore: Store) {
        viewModelScope.launch {
            val updateStore = roomDatabaseHelper.updateStore(newStore)
            savedStore.postValue(updateStore)
            firebaseHelper.updateFirestore(updateStore)

        }
    }


}
