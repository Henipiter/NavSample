package com.example.navsample.viewmodels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.DateUtil
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelperFirebaseSync
import com.example.navsample.entities.dto.CategoryFirebase
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncDatabaseViewModel : ViewModel() {
    private var roomDatabaseHelper: RoomDatabaseHelperFirebaseSync
    private var firebaseHelper: FirebaseHelper


    var productList = MutableLiveData<List<ProductFirebase>>()
    var receiptList = MutableLiveData<List<ReceiptFirebase>>()
    var categoryList = MutableLiveData<List<CategoryFirebase>>()
    var storeList = MutableLiveData<List<StoreFirebase>>()

    init {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        if (myPref?.getString("userId", "") == "") {
            myPref.edit().putString("userId", UUID.randomUUID().toString()).apply()
        }
        val userUuid = myPref?.getString("userId", null) ?: throw Exception("NOT SET userId")
        firebaseHelper = FirebaseHelper(userUuid)

        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelperFirebaseSync(dao)

        loadAllList()
    }

    fun loadAllList() {
        loadStores()
        loadReceipts()
        loadProducts()
        loadCategories()
    }

    fun loadStores() {
        viewModelScope.launch {
            storeList.postValue(roomDatabaseHelper.getAllNotSyncedStores())
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllNotSyncedCategories())
        }
    }

    fun loadReceipts() {
        viewModelScope.launch {
            receiptList.postValue(roomDatabaseHelper.getAllNotSyncedReceipts())
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            productList.postValue(roomDatabaseHelper.getAllNotSyncedProducts())
        }
    }

    fun categoryOperation(category: CategoryFirebase): Boolean {
//        if(category.isSync && !category.upToDate){
//            //TODO UPDATE
//        }
        if (category.firestoreId != "" && category.firestoreId != category.id) {
            updateCategoryFirebaseIdWithDependentStores(category.id)
        } else if (category.id == category.firestoreId) {
            syncCategory(category)
            category.isSync = true
            firebaseHelper.synchronize(category)
            return true
        }
        return false
    }

    fun storeOperation(store: StoreFirebase): Boolean {
//        if(store.isSync && !store.upToDate){
//            //TODO UPDATE
//        }
        if (store.firestoreId != "" && store.firestoreId != store.id) {
            updateStoreFirebaseIdWithDependentReceipts(store.id)
        } else if (store.id == store.firestoreId && store.isCategorySync) {
            syncStore(store)
            store.isSync = true
            firebaseHelper.synchronize(store)
            return true
        }
        return false
    }

    fun receiptOperation(receipt: ReceiptFirebase): Boolean {
//        if(receipt.isSync && !receipt.upToDate){
//            //TODO UPDATE
//        }
        if (receipt.firestoreId != "" && receipt.firestoreId != receipt.id) {
            updateReceiptFirebaseIdWithDependentProducts(receipt.id)
        } else if (receipt.id == receipt.firestoreId && receipt.isStoreSync) {
            syncReceipt(receipt)
            receipt.isSync = true
            firebaseHelper.synchronize(receipt)
            return true
        }
        return false
    }

    fun productOperation(product: ProductFirebase): Boolean {
//        if(product.isSync && !product.upToDate){
//            //TODO UPDATE
//        }
        if (product.id == product.firestoreId && product.isReceiptSync && product.isCategorySync) {
            syncProduct(product)
            product.isSync = true
            firebaseHelper.synchronize(product)
            return true
        }
        return false
    }

    private fun syncCategory(category: CategoryFirebase) {
        val updatedAt = DateUtil.getCurrentUtcTime()
        viewModelScope.launch {
            roomDatabaseHelper.syncCategory(category.id, updatedAt)
        }
    }

    private fun syncStore(store: StoreFirebase) {
        val updatedAt = DateUtil.getCurrentUtcTime()
        viewModelScope.launch {
            roomDatabaseHelper.syncStore(store.id, updatedAt)
        }
    }

    private fun syncReceipt(receipt: ReceiptFirebase) {
        val updatedAt = DateUtil.getCurrentUtcTime()
        viewModelScope.launch {
            roomDatabaseHelper.syncReceipt(receipt.id, updatedAt)
        }
    }

    private fun syncProduct(product: ProductFirebase) {
        val updatedAt = DateUtil.getCurrentUtcTime()
        viewModelScope.launch {
            roomDatabaseHelper.syncProduct(product.id, updatedAt)
        }
    }

    private fun updateCategoryFirebaseIdWithDependentStores(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelper.replaceCategoryWithDependencies(oldId)
            }
        }
    }

    private fun updateStoreFirebaseIdWithDependentReceipts(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelper.replaceStoreWithDependencies(oldId)
            }
        }
    }

    private fun updateReceiptFirebaseIdWithDependentProducts(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelper.replaceReceiptWithDependencies(oldId)
            }
        }
    }


}
