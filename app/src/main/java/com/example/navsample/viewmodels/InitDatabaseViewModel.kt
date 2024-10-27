package com.example.navsample.viewmodels

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch
import java.util.UUID

class InitDatabaseViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper: RoomDatabaseHelper

    var imageUuid = MutableLiveData<String>()
    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>

    lateinit var category: MutableLiveData<Category?>

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
        roomDatabaseHelper = RoomDatabaseHelper(dao!!)
    }

    fun insertProducts(products: List<Product>) {
        Log.i("Database", "insert products. Size: ${products.size}")
        viewModelScope.launch {
            products.forEach { product ->
                val insertedProduct = roomDatabaseHelper.insertProduct(product)
                firebaseHelper.addFirestore(product) {
                    insertedProduct.id = it
                    insertedProduct.fireStoreSync = true
                    viewModelScope.launch {
                        val updatedProduct = roomDatabaseHelper.updateProduct(insertedProduct)
                        firebaseHelper.updateFirestore(updatedProduct)
                    }
                }
            }
        }
    }

    fun insertReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            val insertedReceipt = roomDatabaseHelper.insertReceipt(newReceipt, false)
            firebaseHelper.addFirestore(newReceipt) {
                insertedReceipt.id = it
                insertedReceipt.fireStoreSync = true
                viewModelScope.launch {
                    val updatedReceipt = roomDatabaseHelper.updateReceipt(insertedReceipt)
                    firebaseHelper.updateFirestore(updatedReceipt)
                }
            }
        }
    }

    fun insertCategoryList(category: Category) {
        viewModelScope.launch {
            val insertedCategory = roomDatabaseHelper.insertCategory(category, false)
            firebaseHelper.addFirestore(category) {
                insertedCategory.id = it
                insertedCategory.fireStoreSync = true
                viewModelScope.launch {
                    val updatedCategory = roomDatabaseHelper.updateCategory(insertedCategory)
                    firebaseHelper.updateFirestore(updatedCategory)
                }
            }
        }
    }

    fun insertStore(newStore: Store) {
        viewModelScope.launch {
            val insertedStore = roomDatabaseHelper.insertStore(newStore, false)
            firebaseHelper.addFirestore(newStore) {
                insertedStore.id = it
                insertedStore.fireStoreSync = true
                viewModelScope.launch {
                    val updatedStore = roomDatabaseHelper.updateStore(insertedStore)
                    firebaseHelper.updateFirestore(updatedStore)
                }
            }

        }
    }


}
