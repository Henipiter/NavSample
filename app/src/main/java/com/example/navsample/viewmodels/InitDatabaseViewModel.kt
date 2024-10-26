package com.example.navsample.viewmodels

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
import com.example.navsample.entities.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.UUID

class InitDatabaseViewModel : ViewModel() {

    companion object {
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }


    var userUuid = MutableLiveData<String?>()
    var imageUuid = MutableLiveData<String>()
    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>

    lateinit var category: MutableLiveData<Category?>

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private val firestore = Firebase.firestore

    fun insertProducts(products: List<Product>) {
        Log.i("Database", "insert products. Size: ${products.size}")
        viewModelScope.launch {
            dao?.let {
                products.forEach { product ->
                    Log.i("Database", "insert product: ${product.name}")
                    product.id = UUID.randomUUID().toString()
                    dao.insertProduct(product)
                    addFirestore(product)

                }
            }
        }
    }


    fun insertReceipt(newReceipt: Receipt) {
        Log.i("Database", "insert receipt: ${newReceipt.date} ${newReceipt.pln}")
        viewModelScope.launch {
            dao?.let {
                newReceipt.date = convertDateFormat(newReceipt.date)
                dao.insertReceipt(newReceipt)
            }
            Log.i("Database", "inserted receipt with id ${newReceipt.id}")
            addFirestore(newReceipt)
        }
    }

    private fun convertDateFormat(date: String): String {
        val newDate = date.replace(".", "-")
        val splitDate = newDate.split("-")
        try {
            if (splitDate[2].length == 4) {
                return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0]
            }
            return newDate
        } catch (e: Exception) {
            Log.e("ConvertDate", "Cannot convert date: $splitDate")
            return newDate
        }
    }

    private fun insertUser() {
        val uuid = UUID.randomUUID().toString()
        val user = User(uuid)
        userUuid.value = user.uuid
        user.id = 0
        Log.i("Database", "insert user, uuid: ${user.uuid}")
        viewModelScope.launch {
            try {
                dao?.let {
                    dao.insertUser(user)
                }
            } catch (e: Exception) {
                Log.e("Insert user to DB", e.message.toString())
            }
        }
    }

    fun insertCategoryList(category: Category) {
        Log.i("Database", "insert category ${category.name} - id ${category.id}")
        viewModelScope.launch {
            dao?.insertCategory(category)
        }
    }

    fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                if (uuid == null || uuid == "") {
                    insertUser()
                } else {
                    userUuid.postValue(uuid)
                }
            }
        }
    }

    fun insertStore(newStore: Store) {
        Log.i("Database", "insert store ${newStore.name}")
        viewModelScope.launch {
            try {
                dao?.let {
                    dao.insertStore(newStore)
                }
                Log.i("Database", "inserted receipt with id ${newStore.id}")
                addFirestore(newStore)
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
        }
    }


    private fun getPath(obj: Any): String {
        when (obj) {
            is Store -> {
                return STORE_FIRESTORE_PATH
            }

            is Category -> {
                return CATEGORY_FIRESTORE_PATH
            }

            is Receipt -> {
                return RECEIPT_FIRESTORE_PATH
            }

            is Product -> {
                return PRODUCT_FIRESTORE_PATH
            }
        }
        return "null"
    }

    private fun getFirestoreUserPath(path: String): CollectionReference {
        return firestore.collection("user").document(userUuid.value.toString()).collection(path)

    }

    private fun <T : TranslateEntity> addFirestore(obj: T) {
        getFirestoreUserPath(getPath(obj))
            .document(obj.getDescriptiveId())
            .set(obj)

    }

}
