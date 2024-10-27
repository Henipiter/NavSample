package com.example.navsample.viewmodels.fragment

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

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)


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
            roomDatabaseHelper.deleteStore(storeId)
        }
    }

    fun getStoreById(id: String) {
        viewModelScope.launch {
            roomDatabaseHelper.getStoreById(id)
        }
    }

    fun insertStore(newStore: Store) {
        viewModelScope.launch {
            savedStore.postValue(roomDatabaseHelper.insertStore(newStore))
            firebaseHelper.addFirestore(savedStore.value!!)
        }
    }

    fun updateStore(newStore: Store) {
        viewModelScope.launch {
            roomDatabaseHelper.updateStore(newStore)
            savedStore.postValue(newStore)
            firebaseHelper.updateFirestore(newStore)

        }

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
