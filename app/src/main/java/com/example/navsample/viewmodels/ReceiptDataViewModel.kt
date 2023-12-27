package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.DTO.ChartColors
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.DTO.StoreDTO
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiptDataViewModel : ViewModel() {
    lateinit var store: MutableLiveData<StoreDTO?>
    lateinit var receipt: MutableLiveData<ReceiptDTO?>
    lateinit var product: MutableLiveData<ArrayList<ProductDTO>>
    lateinit var category: MutableLiveData<Category?>

    lateinit var savedStore: MutableLiveData<Store>
    lateinit var savedReceipt: MutableLiveData<Receipt>
    lateinit var savedCategory: MutableLiveData<Category>

    lateinit var productList: MutableLiveData<ArrayList<Product>>
    lateinit var productRichList: MutableLiveData<ArrayList<ProductRichData>>
    lateinit var receiptList: MutableLiveData<ArrayList<ReceiptWithStore>>
    lateinit var categoryList: MutableLiveData<ArrayList<Category>>
    lateinit var storeList: MutableLiveData<ArrayList<Store>>

    lateinit var experimental: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>
    lateinit var experimentalOriginal: MutableLiveData<ArrayList<ExperimentalAdapterArgument>>

    lateinit var chartData: MutableLiveData<ArrayList<PriceByCategory>>
    lateinit var reorderedProductTiles: MutableLiveData<Boolean>

    init {
        clearData()
    }

    fun clearData() {
        reorderedProductTiles = MutableLiveData<Boolean>(false)
        store = MutableLiveData<StoreDTO?>(null)
        receipt = MutableLiveData<ReceiptDTO?>(null)
        product = MutableLiveData<ArrayList<ProductDTO>>(ArrayList())
        category = MutableLiveData<Category?>(null)

        savedStore = MutableLiveData<Store>(null)
        savedReceipt = MutableLiveData<Receipt>(null)
        savedCategory = MutableLiveData<Category>(null)

        productList = MutableLiveData<ArrayList<Product>>(null)
        productRichList = MutableLiveData<ArrayList<ProductRichData>>(null)
        receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>(null)
        categoryList = MutableLiveData<ArrayList<Category>>(null)
        storeList = MutableLiveData<ArrayList<Store>>(null)

        chartData = MutableLiveData<ArrayList<PriceByCategory>>(null)

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

    val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    fun insertProducts(products: List<Product>) {
        viewModelScope.launch {
            dao?.let {
                products.forEach { product ->
                    if (product.id == null) {
                        dao.insertProduct(product)
                    } else {
                        dao.updateProduct(product)
                    }
                }
            }
        }
    }

    fun insertProducts() {
        productList.value?.let { insertProducts(it) }
    }

    fun insertReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            dao?.let {
                newReceipt.date = convertDateFormat(newReceipt.date)
                val rowId = dao.insertReceipt(newReceipt)
                newReceipt.id = dao.getReceiptId(rowId)
            }
            savedReceipt.value = newReceipt
            Log.e("DAO RECEIPT", newReceipt.id.toString())
        }
    }

    fun insertCategory(newCategory: Category) {
        viewModelScope.launch {
            dao?.let {
                val rowId = dao.insertCategory(newCategory)
                newCategory.id = dao.getCategoryId(rowId)
            }
            savedCategory.value = newCategory
            Log.e("DAO RECEIPT", newCategory.id.toString())
        }
    }

    private fun convertDateFormat(date: String): String {
        val date = date.replace(".", "-")
        val splitDate = date.split("-")
        if (splitDate[2].length == 4) {
            return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0]
        }
        return date
    }

    fun insertStore(store: Store) {
        viewModelScope.launch {
            try {
                dao?.let {
                    val rowId = dao.insertStore(store)
                    store.id = dao.getStoreId(rowId)
                }
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
            savedStore.postValue(store)
            refreshStoreList()
            Log.e("DAO STORE", store.id.toString())
        }
    }

    fun updateReceipt(newReceipt: Receipt) {
        viewModelScope.launch {
            dao?.let {
                dao.updateReceipt(newReceipt)
            }
            savedReceipt.postValue(newReceipt)
        }
    }

    fun updateCategory(newCategory: Category) {
        viewModelScope.launch {
            dao?.let {
                dao.updateCategory(newCategory)
            }
            savedCategory.postValue(newCategory)
        }
    }

    fun getStoreById(id: Int) {
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

    fun getStoreByNip(nip: String) {
        viewModelScope.launch {
            try {
                dao?.let {
                    savedStore.postValue(dao.getStoreByNip(nip))
                }
            } catch (e: Exception) {
                Log.e("Insert store to DB", e.message.toString())
            }
        }
    }

    fun updateStore(store: Store) {
        viewModelScope.launch(Dispatchers.IO) {
            dao?.let {
                dao.updateStore(store)
            }
        }
        savedStore.value = store
    }

    fun refreshReceiptList(name: String) {
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getReceiptWithStore(name)?.let { ArrayList(it) })
        }
    }

    fun refreshReceiptList(name: String, dateFrom: String, dateTo: String) {
        viewModelScope.launch {
            receiptList.postValue(
                dao?.getReceiptWithStore(name, dateFrom, dateTo)?.let { ArrayList(it) })
        }
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories() as ArrayList<Category>
            )
        }
    }

    fun refreshStoreList() {
        viewModelScope.launch {
            storeList.postValue(dao?.getAllStores()?.let { ArrayList(it) })
        }
    }

    fun deleteStore(store: Store) {
        viewModelScope.launch {
            dao?.deleteProductsOfStore(store.id!!)
            dao?.deleteReceiptsOfStore(store.id!!)
            dao?.deleteStore(store)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao?.deleteCategory(category)
        }
    }

    fun deleteReceipt(receiptId: Int) {
        viewModelScope.launch {
            dao?.deleteProductsOfReceipt(receiptId)
            dao?.deleteReceiptById(receiptId)
        }
    }

    fun deleteProduct(productId: Int) {
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
        viewModelScope.launch {
            productRichList.postValue(
                dao?.getAllProducts(storeName, categoryName, dateFrom, dateTo, lowerPrice)
                    ?.let { ArrayList(it) })
        }
    }

    fun refreshProductListWithConversion(receiptId: Int) {
        viewModelScope.launch {
            productList.postValue(dao?.getAllProducts(receiptId)?.let { ArrayList(it) })
            convertProductsToDTO()
        }
    }

    fun insertCategoryList(category: Category) {
        viewModelScope.launch {
            dao?.insertCategory(category)
        }
    }

    fun convertProductsToDTO() {
        val newProductDTOs = ArrayList<ProductDTO>()
        productList.value?.forEach { product ->
            val category = getCategory(product.categoryId)
            newProductDTOs.add(
                ProductDTO(
                    product.id,
                    receipt.value?.id ?: product.receiptId,
                    product.name,
                    product.finalPrice.toString(),
                    category.name,
                    category.color,
                    product.amount.toString(),
                    product.itemPrice.toString(),
                    product.ptuType,
                    product.raw
                )
            )
        }
        product.postValue(newProductDTOs)
    }

    fun convertDTOToProduct() {
        val newProducts = ArrayList<Product>()
        val receiptId = savedReceipt.value?.id ?: receipt.value?.id ?: -1
        product.value?.forEach { productDTO ->
            val newProduct = Product(
                receiptId,
                productDTO.name.toString(),
                getCategoryId(productDTO.category.toString()),
                transformToFloat(productDTO.amount.toString()),
                transformToFloat(productDTO.itemPrice.toString()),
                transformToFloat(productDTO.finalPrice.toString()),
                productDTO.ptuType.toString(),
                productDTO.original.toString()
            )
            newProduct.id = productDTO.id
            newProducts.add(newProduct)
        }
        productList.value = newProducts
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

}
