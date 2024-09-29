package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.chart.ChartColors
import com.example.navsample.dto.filter.FilterCategoryList
import com.example.navsample.dto.filter.FilterProductList
import com.example.navsample.dto.filter.FilterReceiptList
import com.example.navsample.dto.filter.FilterStoreList
import com.example.navsample.dto.sort.Direction
import com.example.navsample.dto.sort.ParentSort
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDaoHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.User
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ReceiptDataViewModel : ViewModel() {

    companion object {
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }

    private val defaultStoreSort = SortProperty(StoreSort.NAME, Direction.ASCENDING)
    private val defaultRichProductSort = SortProperty(RichProductSort.DATE, Direction.DESCENDING)
    private val defaultReceiptWithStoreSort =
        SortProperty(ReceiptWithStoreSort.DATE, Direction.DESCENDING)

    lateinit var imageUuid: MutableLiveData<String>

    lateinit var storeSort: MutableLiveData<SortProperty<StoreSort>>
    lateinit var receiptWithStoreSort: MutableLiveData<SortProperty<ReceiptWithStoreSort>>
    lateinit var richProductSort: MutableLiveData<SortProperty<RichProductSort>>

    lateinit var filterCategoryList: MutableLiveData<FilterCategoryList>
    lateinit var filterStoreList: MutableLiveData<FilterStoreList>
    lateinit var filterProductList: MutableLiveData<FilterProductList>
    lateinit var filterReceiptList: MutableLiveData<FilterReceiptList>

    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>
    lateinit var category: MutableLiveData<Category?>

    lateinit var savedStore: MutableLiveData<Store>
    lateinit var savedCategory: MutableLiveData<Category>

    lateinit var productRichList: MutableLiveData<ArrayList<ProductRichData>>
    lateinit var receiptList: MutableLiveData<ArrayList<ReceiptWithStore>>
    lateinit var categoryList: MutableLiveData<ArrayList<Category>>
    lateinit var storeList: MutableLiveData<ArrayList<Store>>


    lateinit var userOrderedName: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var userOrderedPrices: MutableLiveData<ArrayList<UserItemAdapterArgument>>
    lateinit var algorithmOrderedNames: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>
    lateinit var algorithmOrderedPrices: MutableLiveData<ArrayList<AlgorithmItemAdapterArgument>>


    lateinit var timelineChartData: MutableLiveData<List<PriceByCategory>>
    lateinit var categoryChartData: MutableLiveData<List<PriceByCategory>>
    lateinit var allData: MutableLiveData<ArrayList<AllData>>
    lateinit var tableCounts: MutableLiveData<ArrayList<TableCounts>>
    lateinit var reorderedProductTiles: MutableLiveData<Boolean>

    lateinit var userUuid: MutableLiveData<String?>

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private val firestore = Firebase.firestore

    init {
        clearData()
    }

    private fun createFullPath(path: String): String {
        return path
    }

    fun <Sort : ParentSort> updateSorting(sort: SortProperty<Sort>) {
        when (sort.sort) {
            is StoreSort -> {
                filterStoreList.value?.let {
                    refreshStoreList(it.store, it.nip)
                }
            }

            is ReceiptWithStoreSort -> {
                filterReceiptList.value?.let {
                    refreshReceiptList(it.store, it.dateFrom, it.dateTo)
                }
            }

            is RichProductSort -> {
                filterProductList.value?.let {
                    refreshProductList(
                        it.store, it.category, it.dateFrom, it.dateTo, it.lowerPrice, it.higherPrice
                    )
                }
            }
        }
    }

    fun clearData() {
        storeSort = MutableLiveData<SortProperty<StoreSort>>(defaultStoreSort)
        receiptWithStoreSort =
            MutableLiveData<SortProperty<ReceiptWithStoreSort>>(defaultReceiptWithStoreSort)
        richProductSort = MutableLiveData<SortProperty<RichProductSort>>(defaultRichProductSort)
        filterCategoryList = MutableLiveData<FilterCategoryList>(FilterCategoryList())
        filterStoreList = MutableLiveData<FilterStoreList>(FilterStoreList())
        filterProductList = MutableLiveData<FilterProductList>(FilterProductList())
        filterReceiptList = MutableLiveData<FilterReceiptList>(FilterReceiptList())

        imageUuid = MutableLiveData<String>(null)
        reorderedProductTiles = MutableLiveData<Boolean>(false)
        store = MutableLiveData<Store>(null)
        receipt = MutableLiveData<Receipt>(null)
        product = MutableLiveData<ArrayList<Product>>(ArrayList())
        category = MutableLiveData<Category?>(null)

        savedStore = MutableLiveData<Store>(null)
        savedCategory = MutableLiveData<Category>(null)

        productRichList = MutableLiveData<ArrayList<ProductRichData>>(null)
        receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>(null)
        categoryList = MutableLiveData<ArrayList<Category>>(null)
        storeList = MutableLiveData<ArrayList<Store>>(null)

        timelineChartData = MutableLiveData<List<PriceByCategory>>(null)
        categoryChartData = MutableLiveData<List<PriceByCategory>>(null)
        tableCounts = MutableLiveData<ArrayList<TableCounts>>(null)
        allData = MutableLiveData<ArrayList<AllData>>(null)

        userUuid = MutableLiveData<String?>(null)


        refreshCategoryList()
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
                        val path = createFullPath(PRODUCT_FIRESTORE_PATH)
                        addFirestore(path, product.id.toString(), product)
                    } else {
                        Log.i("Database", "update product: ${product.name}")
                        dao.updateProduct(product)
                        val path = createFullPath(PRODUCT_FIRESTORE_PATH)
                        updateFirestore(path, product.id.toString(), product.toMap())
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
            val path = createFullPath(RECEIPT_FIRESTORE_PATH)
            addFirestore(path, newReceipt.id.toString(), newReceipt)
        }
    }

    fun insertCategory(newCategory: Category) {
        Log.i("Database", "insert category: ${newCategory.name}")
        viewModelScope.launch {
            dao?.let {
                val rowId = dao.insertCategory(newCategory)
                newCategory.id = dao.getCategoryId(rowId)
            }
            Log.i("Database", "inserted category with id ${newCategory.id}")
            savedCategory.value = newCategory
            val path = createFullPath(CATEGORY_FIRESTORE_PATH)
            addFirestore(path, newCategory.id.toString(), newCategory)
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
                val path = createFullPath(STORE_FIRESTORE_PATH)
                addFirestore(path, newStore.id.toString(), newStore)
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
            savedStore.postValue(newStore)
            refreshStoreList()
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
            val path = createFullPath(RECEIPT_FIRESTORE_PATH)
            updateFirestore(path, newReceipt.id.toString(), newReceipt.toMap())
        }
    }

    fun updateCategory(newCategory: Category) {
        Log.i("Database", "update category with id ${newCategory.id}: ${newCategory.name}")
        viewModelScope.launch {
            dao?.let {
                dao.updateCategory(newCategory)
            }
            savedCategory.postValue(newCategory)
            val path = createFullPath(CATEGORY_FIRESTORE_PATH)
            updateFirestore(path, newCategory.id.toString(), newCategory.toMap())
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

    fun updateStore(newStore: Store) {
        Log.i("Database", "update store with id ${newStore.id}: ${newStore.name}")
        viewModelScope.launch(Dispatchers.IO) {
            dao?.let {
                dao.updateStore(newStore)
            }
        }
        savedStore.value = newStore
        val path = createFullPath(STORE_FIRESTORE_PATH)
        updateFirestore(path, newStore.id.toString(), newStore.toMap())

    }

    private fun getFirestoreCollection(path: String, id: String): DocumentReference {
        return firestore.collection("user").document(userUuid.value.toString()).collection(path)
            .document(id)
    }

    private fun addFirestore(path: String, id: String, obj: Any) {
        getFirestoreCollection(path, id).set(obj)

    }

    private fun updateFirestore(path: String, id: String, map: Map<String, Any?>) {
        getFirestoreCollection(path, id).update(map)

    }

    fun refreshReceiptList(name: String, dateFrom: String, dateTo: String) {
        Log.i("Database", "refresh receipt for store $name")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getReceiptWithStore(
                dao,
                name,
                if (dateFrom == "") "0" else dateFrom,
                if (dateTo == "") "9" else dateTo,
                receiptWithStoreSort.value ?: defaultReceiptWithStoreSort
            )
            receiptList.postValue(//
                list?.let { ArrayList(it) })
        }
    }

    fun refreshCategoryList() {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories() as ArrayList<Category>
            )
        }
    }

    fun refreshCategoryList(categoryName: String) {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories(categoryName) as ArrayList<Category>
            )
        }
    }

    fun refreshStoreList() {
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
        }
    }

    fun refreshStoreList(name: String, nip: String) {
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getAllStoresOrdered(
                dao, name, nip, storeSort.value ?: defaultStoreSort
            )
            storeList.postValue(list?.let { ArrayList(it) })
        }
    }

    fun deleteStore(store: Store) {
        Log.i("Database", "delete store - id ${store.id}")
        viewModelScope.launch {
            dao?.deleteProductsOfStore(store.id!!)
            dao?.deleteReceiptsOfStore(store.id!!)
            dao?.deleteStore(store)
        }
    }

    fun deleteCategory(category: Category) {
        Log.i("Database", "delete category - id ${category.id}")
        viewModelScope.launch {
            dao?.deleteCategory(category)
        }
    }

    fun deleteReceipt(receiptId: Int) {
        Log.i("Database", "delete receipt - id $receiptId")
        viewModelScope.launch {
            dao?.deleteProductsOfReceipt(receiptId)
            dao?.deleteReceiptById(receiptId)
        }
    }

    fun deleteProduct(productId: Int) {
        Log.i("Database", "delete product - id $productId")
        viewModelScope.launch {
            dao?.deleteProductById(productId)
        }
    }


    fun refreshProductList(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Double,
        higherPrice: Double,
    ) {

        Log.i("Database", "refresh product list limited")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getAllProductsOrdered(
                dao,
                storeName,
                categoryName,
                if (dateFrom == "") "0" else dateFrom,
                if (dateTo == "") "9" else dateTo,
                lowerPrice,
                higherPrice,
                richProductSort.value ?: defaultRichProductSort
            )
            productRichList.postValue(list?.let { ArrayList(it) })
        }
    }

    fun refreshProductList() {
        Log.i("Database", "refresh product list all")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getAllProductsOrdered(
                dao, "", "", "0", "9", 0.0, -1.0, richProductSort.value ?: defaultRichProductSort
            )
            productRichList.postValue(list?.let { ArrayList(it) })
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

    private fun getCategoryId(name: String): Int {
        val categoryNames = categoryList.value?.map { it.name } ?: listOf()
        var categoryIndex = categoryNames.indexOf(name)
        if (categoryIndex == -1) {
            categoryIndex = categoryNames.indexOf("INNE")
        }
        return categoryList.value?.get(categoryIndex)?.id ?: 0
    }

    private fun getCategory(id: Int): Category {
        val categoryNames = categoryList.value?.map { it.id } ?: listOf()
        val categoryIndex = categoryNames.indexOf(id)
        if (categoryIndex == -1) {
            return Category("", ChartColors.DEFAULT_CATEGORY_COLOR_STRING)
        }
        return categoryList.value?.get(categoryIndex) ?: Category(
            "", ChartColors.DEFAULT_CATEGORY_COLOR_STRING
        )
    }

    private fun transformToDouble(value: String): Double {
        return try {
            value.replace(",", ".").toDouble()
        } catch (t: Throwable) {
            0.0
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
