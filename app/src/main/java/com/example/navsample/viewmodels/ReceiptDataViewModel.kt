package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.User
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.TableCounts
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.UUID

class ReceiptDataViewModel : ViewModel() {

    companion object {
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }

    //to delete
    lateinit var categoryList: MutableLiveData<ArrayList<Category>>
    lateinit var storeList: MutableLiveData<ArrayList<Store>>

    //------------------------------
    lateinit var imageUuid: MutableLiveData<String>


    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>
    lateinit var category: MutableLiveData<Category?>

    lateinit var savedStore: MutableLiveData<Store>
    lateinit var savedCategory: MutableLiveData<Category>


    lateinit var userOrderedName: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var userOrderedPrices: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var algorithmOrderedNames: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>
    lateinit var algorithmOrderedPrices: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>


    lateinit var timelineChartData: MutableLiveData<List<PriceByCategory>>
    lateinit var categoryChartData: MutableLiveData<List<PriceByCategory>>
    lateinit var allData: MutableLiveData<ArrayList<AllData>>
    lateinit var tableCounts: MutableLiveData<ArrayList<TableCounts>>
    lateinit var reorderedProductTiles: MutableLiveData<Boolean>

    var userUuid = MutableLiveData<String?>(null)

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private val firestore = Firebase.firestore

    init {
        clearData()
    }


    fun refreshCategoryList() {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories() as ArrayList<Category>
            )
        }
    }


    fun clearData() {
        categoryList = MutableLiveData<ArrayList<Category>>(null)
        storeList = MutableLiveData<ArrayList<Store>>(null)

        //---------------

        imageUuid = MutableLiveData<String>(null)
        reorderedProductTiles = MutableLiveData<Boolean>(false)
        store = MutableLiveData<Store>(null)
        receipt = MutableLiveData<Receipt>(null)
        product = MutableLiveData<ArrayList<Product>>(ArrayList())
        category = MutableLiveData<Category?>(null)

        savedStore = MutableLiveData<Store>(null)
        savedCategory = MutableLiveData<Category>(null)

        timelineChartData = MutableLiveData<List<PriceByCategory>>(null)
        categoryChartData = MutableLiveData<List<PriceByCategory>>(null)
        tableCounts = MutableLiveData<ArrayList<TableCounts>>(null)
        allData = MutableLiveData<ArrayList<AllData>>(null)



        userOrderedName = MutableLiveData(arrayListOf())
        userOrderedPrices = MutableLiveData(arrayListOf())
        algorithmOrderedNames = MutableLiveData(
            arrayListOf(
                AlgorithmItemAdapterArgument("Name100"),
                AlgorithmItemAdapterArgument("Name01"),
                AlgorithmItemAdapterArgument("Name02"),
                AlgorithmItemAdapterArgument("Name03"),
                AlgorithmItemAdapterArgument("Name04"),
                AlgorithmItemAdapterArgument("Name05"),
                AlgorithmItemAdapterArgument("Name06"),
                AlgorithmItemAdapterArgument("Name07"),
                AlgorithmItemAdapterArgument("Name08"),
                AlgorithmItemAdapterArgument("Name09"),
                AlgorithmItemAdapterArgument("Name10"),
                AlgorithmItemAdapterArgument("Name11"),
                AlgorithmItemAdapterArgument("Name12"),
                AlgorithmItemAdapterArgument("Name13"),
                AlgorithmItemAdapterArgument("Name14"),
                AlgorithmItemAdapterArgument("Name15"),
                AlgorithmItemAdapterArgument("Name16"),
                AlgorithmItemAdapterArgument("Name17"),
                AlgorithmItemAdapterArgument("Name18"),
                AlgorithmItemAdapterArgument("Name19")
            )
        )
        algorithmOrderedPrices = MutableLiveData(
            arrayListOf(
                AlgorithmItemAdapterArgument("Price00"),
                AlgorithmItemAdapterArgument("Price01"),
                AlgorithmItemAdapterArgument("Price02"),
                AlgorithmItemAdapterArgument("Price03"),
                AlgorithmItemAdapterArgument("Price04"),
                AlgorithmItemAdapterArgument("Price05"),
                AlgorithmItemAdapterArgument("Price06"),
                AlgorithmItemAdapterArgument("Price07"),
                AlgorithmItemAdapterArgument("Price08"),
                AlgorithmItemAdapterArgument("Price09"),
                AlgorithmItemAdapterArgument("Price10"),
                AlgorithmItemAdapterArgument("Price11"),
                AlgorithmItemAdapterArgument("Price12"),
                AlgorithmItemAdapterArgument("Price13"),
                AlgorithmItemAdapterArgument("Price14"),
                AlgorithmItemAdapterArgument("Price15"),
                AlgorithmItemAdapterArgument("Price16"),
                AlgorithmItemAdapterArgument("Price17"),
                AlgorithmItemAdapterArgument("Price18"),
                AlgorithmItemAdapterArgument("Price19"),
            )
        )

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


    fun insertReceipt(newReceipt: Receipt) {
        Log.i("Database", "insert receipt: ${newReceipt.date} ${newReceipt.pln}")
        viewModelScope.launch {
            dao?.let {
                newReceipt.date = convertDateFormat(newReceipt.date)
                val rowId = dao.insertReceipt(newReceipt)
                newReceipt.id = dao.getReceiptId(rowId)
            }
            Log.i("Database", "inserted receipt with id ${newReceipt.id}")
            receipt.value = newReceipt
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
                //addFirestore(STORE_FIRESTORE_PATH, newStore.id.toString(), newStore)
            } catch (e: Exception) {
                Log.e("Insert user to DB", e.message.toString())
            }
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

    fun updateReceipt(newReceipt: Receipt) {
        Log.i(
            "Database",
            "update receipt with id ${newReceipt.id}: ${newReceipt.date} ${newReceipt.pln}"
        )
        viewModelScope.launch {
            dao?.let {
                dao.updateReceipt(newReceipt)
            }
            receipt.postValue(newReceipt)
            updateFirestore(newReceipt)
        }
    }

    fun getStoreById(id: Int) {
        Log.i("Database", "get store with id $id")
        viewModelScope.launch {
            dao?.let {
                store.postValue(dao.getStoreById(id))
            }
        }
    }

    fun getReceiptById(id: Int) {
        Log.i("Database", "get receipt with id $id")
        viewModelScope.launch {
            dao?.let {
                receipt.postValue(dao.getReceipt(id))
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

    private fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath(getPath(obj))
            .document(obj.getDescriptiveId())
            .update(obj.toMap())

    }


    fun deleteCategory(category: Category) {
        Log.i("Database", "delete category - id ${category.id}")
        viewModelScope.launch {
            dao?.deleteCategory(category)
        }
    }

    fun deleteProduct(productId: Int) {
        Log.i("Database", "delete product - id $productId")
        viewModelScope.launch {
            dao?.deleteProductById(productId)
        }
    }


    fun refreshProductListForReceipt(receiptId: Int) {
        viewModelScope.launch {
            product.postValue(dao?.getAllProducts(receiptId)?.let { ArrayList(it) })
        }
    }

    fun insertCategoryList(category: Category) {
        Log.i("Database", "insert category ${category.name} - id ${category.id}")
        viewModelScope.launch {
            dao?.insertCategory(category)
        }
    }

    fun getChartDataTimeline(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            timelineChartData.postValue(dao?.getPricesForCategoryComparisonWithDate(
                dateFrom, dateTo
            )?.let { ArrayList(it) })
        }
    }

    fun getChartDataCategory(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            categoryChartData.postValue(dao?.getPricesForCategoryComparison(dateFrom, dateTo)
                ?.let { ArrayList(it) })
        }
    }

    fun getTableCounts() {
        viewModelScope.launch {
            tableCounts.postValue(dao?.getTableCounts()?.let { ArrayList(it) })
        }
    }

    fun getAllData() {
        viewModelScope.launch {
            allData.postValue(dao?.getAllData()?.let { ArrayList(it) })
        }
    }
}
