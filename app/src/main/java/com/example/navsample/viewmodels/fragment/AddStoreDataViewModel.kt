package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.TranslateEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddStoreDataViewModel : ViewModel() {

    companion object {
        private const val CATEGORY_FIRESTORE_PATH = "stores"
    }

    private val firestore = Firebase.firestore
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    var storeList = MutableLiveData<ArrayList<Store>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var storeById = MutableLiveData<Store?>()
    var savedStore = MutableLiveData<Store>()
    var userUuid = MutableLiveData<String?>(null)

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

    fun deleteStore(storeId: Int) {
        Log.i("Database", "delete store - id ${storeId}")
        viewModelScope.launch {
            dao?.deleteProductsOfStore(storeId)
            dao?.deleteReceiptsOfStore(storeId)
            dao?.deleteStoreById(storeId)
        }
    }

    fun getStoreById(id: Int) {
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
                dao?.let {
                    val rowId = dao.insertStore(newStore)
                    newStore.id = dao.getStoreId(rowId)
                }
                Log.i("Database", "inserted receipt with id ${newStore.id}")
                addFirestore(newStore)
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
            savedStore.postValue(newStore)
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
        updateFirestore(newStore)

    }

    private fun <T : TranslateEntity> addFirestore(obj: T) {
        getFirestoreUserPath().document(obj.getDescriptiveId()).set(obj)
    }

    private fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath()
            .document(obj.getDescriptiveId())
            .update(obj.toMap())

    }

    private fun getFirestoreUserPath(): CollectionReference {
        return firestore.collection("user").document(userUuid.value.toString())
            .collection(CATEGORY_FIRESTORE_PATH)

    }

    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
            }
        }
    }


}
