package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddStoreDataViewModel : ViewModel() {

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper


    var inputType = AddingInputType.EMPTY.name
    var storeId = ""
    var categoryId = ""
    var storeName: String? = null
    var storeNip: String? = null

    var storeList = MutableLiveData<ArrayList<Store>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var storeById = MutableLiveData<Store?>()
    var savedStore = MutableLiveData<Store>()
    private var userUuid = MutableLiveData<String?>(null)

    init {
        setUserUuid()
    }

    fun refreshCategoryList() {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories() as ArrayList<Category>
            )
        }
    }

    fun refreshStoreList() {
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
        }
    }

    fun deleteStore(storeId: String) {
        Log.i("Database", "delete store - id $storeId")
        viewModelScope.launch {
            dao?.deleteProductsOfStore(storeId)
            dao?.deleteReceiptsOfStore(storeId)
            dao?.deleteStoreById(storeId)
        }
    }

    fun getStoreById(id: String) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                storeById.postValue(dao.getStoreById(id))
            }
        }
    }

    fun insertStore(newStore: Store) {
        Log.i("Database", "insert store ${newStore.name}")
        viewModelScope.launch {
            try {
                newStore.id = UUID.randomUUID().toString()
                dao?.let {
                    dao.insertStore(newStore)
                }
                Log.i("Database", "inserted receipt with id ${newStore.id}")
                firebaseHelper.addFirestore(newStore)
                savedStore.postValue(newStore)
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
        }
    }

    fun updateStore(newStore: Store) {
        Log.i("Database", "update store with id ${newStore.id}: ${newStore.name}")
        viewModelScope.launch(Dispatchers.IO) {
            dao?.let {
                dao.updateStore(newStore)
            }
        }
        savedStore.value = newStore
        firebaseHelper.updateFirestore(newStore)

    }


    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
                firebaseHelper = FirebaseHelper(uuid!!)
            }
        }
    }


}
