package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.relations.ReceiptWithStore
import kotlinx.coroutines.launch

class ReceiptDataViewModel : ViewModel() {
    lateinit var receipt: MutableLiveData<ReceiptDTO?>
    lateinit var product: MutableLiveData<ArrayList<ProductDTO>>

    lateinit var savedStore: MutableLiveData<Store>
    lateinit var savedReceipt: MutableLiveData<Receipt>
    lateinit var savedProduct: MutableLiveData<ArrayList<Product>>

    lateinit var receiptList: MutableLiveData<ArrayList<ReceiptWithStore>>
    lateinit var categoryList: MutableLiveData<ArrayList<String>>
    lateinit var storeList: MutableLiveData<ArrayList<Store>>

    lateinit var experimental: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>
    lateinit var experimentalOriginal: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>
    fun clearData() {
        receipt = MutableLiveData<ReceiptDTO?>(null)
        product = MutableLiveData<ArrayList<ProductDTO>>(ArrayList())

        savedStore = MutableLiveData<Store>(null)
        savedReceipt = MutableLiveData<Receipt>(null)
        savedProduct = MutableLiveData<ArrayList<Product>>(null)

        receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>(null)
        categoryList = MutableLiveData<ArrayList<String>>(null)
        storeList = MutableLiveData<ArrayList<Store>>(null)

        experimental = MutableLiveData(
            arrayListOf(
                ExperimentalAdapterArgument("01"),
                ExperimentalAdapterArgument("02"),
                ExperimentalAdapterArgument("03"),
                ExperimentalAdapterArgument("04")
            )
        )
        experimentalOriginal = MutableLiveData(
            arrayListOf(
                ExperimentalAdapterArgument("01"),
                ExperimentalAdapterArgument("02"),
                ExperimentalAdapterArgument("03"),
                ExperimentalAdapterArgument("04")
            )
        )
    }

    val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    fun insertReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            dao?.let {
                val rowId = dao.insertReceipt(newReceipt)
                newReceipt.id = dao.getReceiptId(rowId)
            }
            savedReceipt.value = newReceipt
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            dao?.let {
                dao.updateReceipt(newReceipt)
            }
            savedReceipt.value = newReceipt
        }
    }

    fun insertStore(store: Store) {
        viewModelScope.launch {
            try {
                dao?.let {
                    val rowId = dao.insertStore(store)
                    store.id = dao.getStoreId(rowId)
                }
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
        }
        savedStore.value = store
    }

    fun updateStore(store: Store) {
        viewModelScope.launch {
            dao?.let {

                dao.updateStore(store)

            }
        }
        savedStore.value = store
    }

    fun refreshReceiptList(name: String) {
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getReceiptWithStore(name)?.let { ArrayList(it) })
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

    fun refreshProductList(receiptId: Int) {
        viewModelScope.launch {
            savedProduct.postValue(dao?.getAllProducts(receiptId)?.let { ArrayList(it) })
        }
    }

    fun insertCategoryList(category: Category) {
        viewModelScope.launch {
            dao?.insertCategory(category)
        }
    }

    init {
        clearData()
    }

}
