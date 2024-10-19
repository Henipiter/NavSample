package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.TranslateEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AddProductDataViewModel : ViewModel() {

    companion object {
        private const val PRODUCT_FIRESTORE_PATH = "products"
    }

    private val firestore = Firebase.firestore
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    var categoryList = MutableLiveData<List<Category>>()
    var productList = MutableLiveData<MutableList<Product>>()
    var receiptById = MutableLiveData<Receipt?>()
    var productById = MutableLiveData<Product?>()
    var storeById = MutableLiveData<Store?>()
    private var userUuid = MutableLiveData<String?>(null)

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


    fun deleteProduct(productId: Int) {
        Log.i("Database", "delete product - id $productId")
        viewModelScope.launch {
            dao?.deleteProductById(productId)
        }
    }

    fun getReceiptById(id: Int) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                receiptById.postValue(dao.getReceiptById(id))
            }
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

    fun getProductById(id: Int) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                productById.postValue(dao.getProductById(id))
            }
        }
    }

    fun getProductsByReceiptId(receiptId: Int) {
        Log.i("Database", "get store with id $receiptId")
        viewModelScope.launch {
            dao?.let { dao ->
                productList.postValue(dao.getAllProducts(receiptId).toMutableList())
            }
        }
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            dao?.let {
                Log.i("Database", "update product: ${product.name}")
                dao.updateProduct(product)
                updateFirestore(product)
            }
        }
    }

    fun insertProducts(products: List<Product>) {
        Log.i("Database", "insert products. Size: ${products.size}")
        viewModelScope.launch {
            dao?.let {
                products.forEach { product ->
                    if (product.id == null) {
                        Log.i("Database", "insert product: ${product.name}")
                        val rowId = dao.insertProduct(product)
                        product.id = dao.getProductId(rowId)
                        addFirestore(product)
                    } else {
                        Log.i("Database", "update product: ${product.name}")
                        dao.updateProduct(product)
                        updateFirestore(product)
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
            updateFirestore(newReceipt)
        }
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
            .collection(PRODUCT_FIRESTORE_PATH)

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
