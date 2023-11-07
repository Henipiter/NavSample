package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch

class ReceiptDataViewModel : ViewModel() {
    var receipt = MutableLiveData<ReceiptDTO?>(null)
    var product = MutableLiveData<ArrayList<ProductDTO>>(ArrayList())

    var savedStore = MutableLiveData<Store>(null)
    var savedReceipt = MutableLiveData<Receipt>(null)
    var savedProduct = MutableLiveData<ArrayList<Product>>(null)

    var receiptList = MutableLiveData<ArrayList<Receipt>>(null)
    var categoryList = MutableLiveData<ArrayList<String>>(null)
    var storeList = MutableLiveData<ArrayList<Store>>(null)

    val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    fun refreshReceiptList() {
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getAllReceipts()?.let { ArrayList(it) })
        }
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories()?.let { it2 -> ArrayList(it2.map { it.name }) })
        }
    }

    fun refreshStoreList() {
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
        }
    }

    fun insertCategoryList(category: Category) {
        viewModelScope.launch {
            dao?.insertCategory(category)
        }
    }

}
