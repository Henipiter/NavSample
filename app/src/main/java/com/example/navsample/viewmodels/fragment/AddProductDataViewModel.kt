package com.example.navsample.viewmodels.fragment

import android.util.Log
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
import com.example.navsample.entities.Store
import kotlinx.coroutines.launch
import java.util.UUID

class AddProductDataViewModel : ViewModel() {

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper

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
    private var userUuid = MutableLiveData<String?>(null)
    var cropImageFragmentOnStart = true

    init {
        setUserUuid()
    }

    fun refreshCategoryList() {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories()
            )
        }
    }


    fun deleteProduct(productId: String) {
        Log.i("Database", "delete product - id $productId")
        viewModelScope.launch {
            dao?.deleteProductById(productId)
        }
    }

    fun getReceiptById(id: String) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                receiptById.postValue(dao.getReceiptById(id))
            }
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

    fun getProductById(id: String) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                productById.postValue(dao.getProductById(id))
            }
        }
    }

    fun getProductsByReceiptId(receiptId: String) {
        Log.i("Database", "get store with id $receiptId")
        viewModelScope.launch {
            dao?.let { dao ->
                productList.postValue(dao.getAllProducts(receiptId) as ArrayList)
            }
        }
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            dao?.let {
                Log.i("Database", "update product: ${product.name}")
                dao.updateProduct(product)
                firebaseHelper.updateFirestore(product)
            }
        }
    }

    fun insertProducts(products: List<Product>) {
        Log.i("Database", "insert products. Size: ${products.size}")
        viewModelScope.launch {
            dao?.let {
                products.forEach { product ->
                    if (product.id.isEmpty()) {
                        product.id = UUID.randomUUID().toString()
                        Log.i("Database", "insert product: ${product.name}")
                        dao.insertProduct(product)
                        firebaseHelper.addFirestore(product)
                    } else {
                        Log.i("Database", "update product: ${product.name}")
                        dao.updateProduct(product)
                        firebaseHelper.updateFirestore(product)
                    }
                }
            }
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        Log.i(
            "Database",
            "update receipt with id ${newReceipt.id}: ${newReceipt.date} ${newReceipt.pln}"
        )
        viewModelScope.launch {
            dao?.let {
                dao.updateReceipt(newReceipt)
            }
            firebaseHelper.updateFirestore(newReceipt)
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
