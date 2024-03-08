package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.DTO.ChartColors
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.DTO.StoreDTO
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiptDataViewModel : ViewModel() {
    lateinit var store: MutableLiveData<StoreDTO?>
    lateinit var receipt: MutableLiveData<ReceiptDTO?>
    lateinit var product: MutableLiveData<ArrayList<Product>>
    lateinit var category: MutableLiveData<Category?>

    lateinit var savedStore: MutableLiveData<Store>
    lateinit var savedReceipt: MutableLiveData<Receipt>
    lateinit var savedCategory: MutableLiveData<Category>

    lateinit var productRichList: MutableLiveData<ArrayList<ProductRichData>>
    lateinit var receiptList: MutableLiveData<ArrayList<ReceiptWithStore>>
    lateinit var categoryList: MutableLiveData<ArrayList<Category>>
    lateinit var storeList: MutableLiveData<ArrayList<Store>>

    lateinit var experimental: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>
    lateinit var experimentalOriginal: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>

    lateinit var chartData: MutableLiveData<ArrayList<PriceByCategory>>
    lateinit var allData: MutableLiveData<ArrayList<AllData>>
    lateinit var tableCounts: MutableLiveData<ArrayList<TableCounts>>
    lateinit var reorderedProductTiles: MutableLiveData<Boolean>

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    init {
        clearData()
    }

    fun clearData() {
        reorderedProductTiles = MutableLiveData<Boolean>(false)
        store = MutableLiveData<StoreDTO?>(null)
        receipt = MutableLiveData<ReceiptDTO?>(null)
        product = MutableLiveData<ArrayList<Product>>(ArrayList())
        category = MutableLiveData<Category?>(null)

        savedStore = MutableLiveData<Store>(null)
        savedReceipt = MutableLiveData<Receipt>(null)
        savedCategory = MutableLiveData<Category>(null)

        productRichList = MutableLiveData<ArrayList<ProductRichData>>(null)
        receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>(null)
        categoryList = MutableLiveData<ArrayList<Category>>(null)
        storeList = MutableLiveData<ArrayList<Store>>(null)

        chartData = MutableLiveData<ArrayList<PriceByCategory>>(null)
        tableCounts = MutableLiveData<ArrayList<TableCounts>>(null)
        allData = MutableLiveData<ArrayList<AllData>>(null)


        refreshCategoryList()
        experimental = MutableLiveData(
            arrayListOf(
                ExperimentalAdapterArgument("01"),
                ExperimentalAdapterArgument("02"),
                ExperimentalAdapterArgument("03"),
                ExperimentalAdapterArgument("04")
            )
        )
        experimentalOriginal = MutableLiveData(
            arrayListOf(
                ExperimentalAdapterArgument("01"),
                ExperimentalAdapterArgument("02"),
                ExperimentalAdapterArgument("03"),
                ExperimentalAdapterArgument("04")
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
                        dao.insertProduct(product)
                    } else {
                        Log.i("Database", "update product: ${product.name}")
                        dao.updateProduct(product)
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
            savedReceipt.value = newReceipt
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
            Log.e("ConvertDate", "Cannot convert date: " + splitDate)
            return newDate
        }
    }

    fun insertStore(store: Store) {
        Log.i("Database", "insert store ${store.name}")
        viewModelScope.launch {
            try {
                dao?.let {
                    val rowId = dao.insertStore(store)
                    store.id = dao.getStoreId(rowId)
                }
                Log.i("Database", "inserted receipt with id ${store.id}")
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
            savedStore.postValue(store)
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
            savedReceipt.postValue(newReceipt)
        }
    }

    fun updateCategory(newCategory: Category) {
        Log.i("Database", "update category with id ${newCategory.id}: ${newCategory.name}")
        viewModelScope.launch {
            dao?.let {
                dao.updateCategory(newCategory)
            }
            savedCategory.postValue(newCategory)
        }
    }

    fun getStoreById(id: Int) {
        Log.i("Database", "get store with id ${id}")
        viewModelScope.launch {
            try {
                dao?.let {
                    savedStore.postValue(dao.getStoreById(id))
                }
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
        }
    }

    fun updateStore(store: Store) {
        Log.i("Database", "update store with id ${store.id}: ${store.name}")
        viewModelScope.launch(Dispatchers.IO) {
            dao?.let {
                dao.updateStore(store)
            }
        }
        savedStore.value = store
    }

    fun refreshReceiptList(name: String) {
        Log.i("Database", "refresh receipt for store ${name}")
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getReceiptWithStore(name)?.let { ArrayList(it) })
        }
    }

    fun refreshReceiptList(name: String, dateFrom: String, dateTo: String) {
        Log.i("Database", "refresh receipt for store ${name}")
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getReceiptWithStore(name, dateFrom, dateTo)?.let { ArrayList(it) })
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

    fun refreshStoreList() {
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
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
        Log.i("Database", "delete receipt - id ${receiptId}")
        viewModelScope.launch {
            dao?.deleteProductsOfReceipt(receiptId)
            dao?.deleteReceiptById(receiptId)
        }
    }

    fun deleteProduct(productId: Int) {
        Log.i("Database", "delete product - id ${productId}")
        viewModelScope.launch {
            dao?.deleteProductById(productId)
        }
    }


    fun refreshProductList(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Float,
        higherPrice: Float,
    ) {
        Log.i("Database", "refresh product list limited")
        viewModelScope.launch {
            productRichList.postValue(
                dao?.getAllProducts(
                    storeName,
                    categoryName,
                    dateFrom,
                    dateTo,
                    lowerPrice,
                    higherPrice
                )?.let { ArrayList(it) })
        }
    }

    fun refreshProductList() {
        Log.i("Database", "refresh product list all")
        viewModelScope.launch {
            productRichList.postValue(
                dao?.getAllProducts("", "", "0", "9", 0F)?.let { ArrayList(it) })
        }
    }

    fun refreshProductList(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Float,
    ) {
        Log.i("Database", "refresh product list not limited")
        viewModelScope.launch {
            productRichList.postValue(
                dao?.getAllProducts(storeName, categoryName, dateFrom, dateTo, lowerPrice)
                    ?.let { ArrayList(it) })
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
            "",
            ChartColors.DEFAULT_CATEGORY_COLOR_STRING
        )
    }

    private fun transformToFloat(value: String): Float {
        return try {
            value.replace(",", ".").toFloat()
        } catch (t: Throwable) {
            0.0f
        }
    }

    fun getChartDataTimeline(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            chartData.postValue(
                dao?.getPricesForCategoryComparisonWithDate(dateFrom, dateTo)
                    ?.let { ArrayList(it) })
        }
    }

    fun getChartDataCategory(dateFrom: String = "0", dateTo: String = "9") {
        viewModelScope.launch {
            chartData.postValue(
                dao?.getPricesForCategoryComparison(dateFrom, dateTo)?.let { ArrayList(it) })
        }
    }

    fun getTableCounts() {
        viewModelScope.launch {
            tableCounts.postValue(
                dao?.getTableCounts()?.let { ArrayList(it) })
        }
    }

    fun getAllData() {
        viewModelScope.launch {
            allData.postValue(
                dao?.getAllData()?.let { ArrayList(it) })
        }
    }
}
