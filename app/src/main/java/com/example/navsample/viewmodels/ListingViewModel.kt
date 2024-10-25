package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
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
import com.example.navsample.entities.Category
import com.example.navsample.entities.ReceiptDaoHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.Store
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import kotlinx.coroutines.launch

class ListingViewModel : ViewModel() {
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }


    val defaultStoreSort = SortProperty<StoreSort>(StoreSort.NAME, Direction.ASCENDING)
    val defaultRichProductSort =
        SortProperty<RichProductSort>(RichProductSort.DATE, Direction.DESCENDING)
    val defaultReceiptWithStoreSort =
        SortProperty<ReceiptWithStoreSort>(ReceiptWithStoreSort.DATE, Direction.DESCENDING)

    val storeSort = MutableLiveData(defaultStoreSort)
    val receiptWithStoreSort = MutableLiveData(defaultReceiptWithStoreSort)
    val richProductSort = MutableLiveData(defaultRichProductSort)


    val filterCategoryList = MutableLiveData(FilterCategoryList())
    val filterStoreList = MutableLiveData(FilterStoreList())
    val filterProductList = MutableLiveData(FilterProductList())
    val filterReceiptList = MutableLiveData(FilterReceiptList())

    var productRichList = MutableLiveData<ArrayList<ProductRichData>>()
    var receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var storeList = MutableLiveData<ArrayList<Store>>()

    init {
        storeSort.value = defaultStoreSort
        receiptWithStoreSort.value = defaultReceiptWithStoreSort
        richProductSort.value = defaultRichProductSort
        loadDataByStoreFilter()
        loadDataByReceiptFilter()
        loadDataByProductFilter()
        loadDataByCategoryFilter()
    }

    fun <Sort : ParentSort> updateSorting(sort: SortProperty<Sort>) {
        when (sort.sort) {
            is StoreSort -> {
                loadDataByStoreFilter()
            }

            is ReceiptWithStoreSort -> {
                loadDataByReceiptFilter()
            }

            is RichProductSort -> {
                loadDataByProductFilter()
            }
        }
    }

    fun loadDataByStoreFilter() {
        filterStoreList.value?.let {
            refreshStoreList(it.store, it.nip)
        }
    }

    fun loadDataByCategoryFilter() {
        filterCategoryList.value?.let {
            refreshCategoryList(it.category)
        }
    }

    fun loadDataByReceiptFilter() {
        filterReceiptList.value?.let {
            refreshReceiptList(
                it.store, it.dateFrom, it.dateTo
            )
        }
    }

    fun loadDataByProductFilter() {
        filterProductList.value?.let {
            refreshProductList(
                it.store,
                it.category,
                it.dateFrom,
                it.dateTo,
                it.lowerPrice,
                it.higherPrice
            )
        }
    }

    private fun refreshReceiptList(name: String, dateFrom: String, dateTo: String) {
        Log.i("Database", "refresh receipt for store $name")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getReceiptWithStore(
                dao,
                name,
                if (dateFrom == "") "0" else dateFrom,
                if (dateTo == "") "9" else dateTo,
                receiptWithStoreSort.value ?: defaultReceiptWithStoreSort
            )
            receiptList.postValue(list?.let { ArrayList(it) })
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

    private fun refreshCategoryList(categoryName: String) {
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

    private fun refreshStoreList(name: String, nip: String) {
        Log.i("Database", "refresh store list")
        viewModelScope.launch {
            val list = ReceiptDaoHelper.getAllStoresOrdered(
                dao, name, nip, storeSort.value ?: defaultStoreSort
            )
            storeList.postValue(list?.let { ArrayList(it) })
        }
    }

    private fun refreshProductList(
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
}
