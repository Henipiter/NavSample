package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.DateUtil
import com.example.navsample.entities.Category
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelperFirebaseSync
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncDatabaseViewModel : ViewModel() {
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private var roomDatabaseHelper = RoomDatabaseHelperFirebaseSync(dao!!)


    var productList = MutableLiveData<List<ProductFirebase>>()
    var receiptList = MutableLiveData<List<ReceiptFirebase>>()
    var categoryList = MutableLiveData<List<Category>>()
    var storeList = MutableLiveData<List<StoreFirebase>>()

    init {
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
            storeList.postValue(roomDatabaseHelper.getStoreForFirestore())
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllNotSyncedCategories())
        }
    }

    fun loadReceipts() {
        viewModelScope.launch {
            receiptList.postValue(roomDatabaseHelper.getReceiptForFirestore())
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            productList.postValue(roomDatabaseHelper.getProductForFirestore())
        }
    }

    fun syncCategory(category: Category) {
        if (category.id == category.firestoreId) {
            val updatedAt = DateUtil.getCurrentUtcTime()
            viewModelScope.launch {
                roomDatabaseHelper.syncCategory(category.id, updatedAt)
            }
        }
    }

    fun syncStore(store: StoreFirebase) {
        if (store.id == store.firestoreId && store.isCategorySync) {
            val updatedAt = DateUtil.getCurrentUtcTime()
            viewModelScope.launch {
                roomDatabaseHelper.syncStore(store.id, updatedAt)
            }
        }
    }

    fun syncReceipt(receipt: ReceiptFirebase) {
        if (receipt.id == receipt.firestoreId && receipt.isStoreSync) {
            val updatedAt = DateUtil.getCurrentUtcTime()
            viewModelScope.launch {
                roomDatabaseHelper.syncReceipt(receipt.id, updatedAt)
            }
        }
    }

    fun syncProduct(product: ProductFirebase) {
        if (product.id == product.firestoreId && product.isReceiptSync) {
            val updatedAt = DateUtil.getCurrentUtcTime()
            viewModelScope.launch {
                roomDatabaseHelper.syncProduct(product.id, updatedAt)
            }
        }
    }


    fun updateCategoryFirebaseIdWithDependentStores(oldId: String, newCategory: Category) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelper.replaceCategoryWithDependencies(oldId, newCategory)
                loadStores()
            }
        }
    }


}
