package com.example.navsample.viewmodels.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch

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

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            val deletedProducts = roomDatabaseHelper.deleteStoreProducts(storeId)
            firebaseHelper.delete(deletedProducts) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
            val deletedReceipts = roomDatabaseHelper.deleteStoreReceipts(storeId)
            firebaseHelper.delete(deletedReceipts) { id ->
                viewModelScope.launch { roomDatabaseHelper.markReceiptAsDeleted(id) }
            }
            val deletedStore = roomDatabaseHelper.deleteStore(storeId)
            firebaseHelper.delete(deletedStore) { id ->
                viewModelScope.launch { roomDatabaseHelper.markStoreAsDeleted(id) }
            }
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
                firebaseHelper.updateFirestore(updateStore) {
                    viewModelScope.launch { roomDatabaseHelper.markStoreAsUpdated(newStore.id) }
                }
            }
        }
    }


}
