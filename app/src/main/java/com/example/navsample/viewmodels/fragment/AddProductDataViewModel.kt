package com.example.navsample.viewmodels.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch

class AddProductDataViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var productIndex = -1
    var productId = ""
    var receiptId = ""
    var storeId = ""
    var categoryId = ""

    var categoryList = MutableLiveData<List<Category>>()
    var productList = MutableLiveData<ArrayList<Product>>()
    var receiptById = MutableLiveData<Receipt?>()
    var productById = MutableLiveData<Product?>()
    var storeById = MutableLiveData<Store?>()
    var cropImageFragmentOnStart = true

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
            categoryList.postValue(roomDatabaseHelper.getAllCategories())
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val deletedProduct = roomDatabaseHelper.deleteProductById(productId)
            firebaseHelper.delete(deletedProduct)
        }
    }

    fun getReceiptById(id: String) {
        viewModelScope.launch {
            receiptById.postValue(roomDatabaseHelper.getReceiptById(id))
        }
    }

    fun getStoreById(id: String) {
        viewModelScope.launch {
            storeById.postValue(roomDatabaseHelper.getStoreById(id))
        }
    }

    fun getProductById(id: String) {
        viewModelScope.launch {
            productById.postValue(roomDatabaseHelper.getProductById(id))
        }
    }

    fun getProductsByReceiptId(receiptId: String) {
        viewModelScope.launch {
            productList.postValue(roomDatabaseHelper.getProductsByReceiptId(receiptId) as ArrayList)
        }
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            val updatedProduct = roomDatabaseHelper.updateProduct(product)
            firebaseHelper.updateFirestore(updatedProduct)
        }
    }

    fun insertProducts(products: List<Product>) {
        products.forEach { product ->
            if (product.id.isEmpty()) {
                viewModelScope.launch {
                    val savedProduct = roomDatabaseHelper.insertProduct(product)
                    firebaseHelper.addFirestore(savedProduct) {
                        savedProduct.firestoreId = it
                        updateSingleProduct(savedProduct)
                    }
                }
            } else {
                viewModelScope.launch {
                    val updatedProduct = roomDatabaseHelper.updateProduct(product)
                    firebaseHelper.updateFirestore(updatedProduct)
                }
            }
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            val updatedReceipt = roomDatabaseHelper.updateReceipt(newReceipt)
            firebaseHelper.updateFirestore(updatedReceipt)
        }
    }


}
