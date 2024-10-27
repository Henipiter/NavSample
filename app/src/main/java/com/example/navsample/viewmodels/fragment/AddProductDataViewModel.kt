package com.example.navsample.viewmodels.fragment

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

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)

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
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories())
        }
    }


    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            roomDatabaseHelper.deleteProductById(productId)
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
            roomDatabaseHelper.updateProduct(product)
            firebaseHelper.updateFirestore(product)
        }
    }

    fun insertProducts(products: List<Product>) {
        products.forEach { product ->
            if (product.id.isEmpty()) {
                viewModelScope.launch {
                    val savedProduct = roomDatabaseHelper.insertProduct(product)
                    firebaseHelper.addFirestore(savedProduct)
                }
            } else {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProduct(product)
                    firebaseHelper.updateFirestore(product)
                }
            }
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            roomDatabaseHelper.updateReceipt(newReceipt)
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
