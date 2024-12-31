package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.filter.FilterCategoryList
import com.example.navsample.dto.filter.FilterProductList
import com.example.navsample.dto.filter.FilterReceiptList
import com.example.navsample.dto.filter.FilterStoreList
import com.example.navsample.dto.filter.FilterTagList
import com.example.navsample.dto.sort.Direction
import com.example.navsample.dto.sort.ParentSort
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import kotlinx.coroutines.launch

class ListingViewModel : ViewModel() {
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)

    val defaultStoreSort = SortProperty<StoreSort>(StoreSort.NAME, Direction.ASCENDING)
    val defaultRichProductSort =
        SortProperty<RichProductSort>(RichProductSort.DATE, Direction.DESCENDING)
    val defaultReceiptWithStoreSort =
        SortProperty<ReceiptWithStoreSort>(ReceiptWithStoreSort.DATE, Direction.DESCENDING)

    val storeSort = MutableLiveData(defaultStoreSort)
    val receiptWithStoreSort = MutableLiveData(defaultReceiptWithStoreSort)
    val richProductSort = MutableLiveData(defaultRichProductSort)


    val filterTagList = MutableLiveData(FilterTagList())
    val filterCategoryList = MutableLiveData(FilterCategoryList())
    val filterStoreList = MutableLiveData(FilterStoreList())
    val filterProductList = MutableLiveData(FilterProductList())
    val filterReceiptList = MutableLiveData(FilterReceiptList())

    var productRichList = MutableLiveData<ArrayList<ProductRichData>>()
    var receiptList = MutableLiveData<ArrayList<ReceiptWithStore>>()
    var categoryList = MutableLiveData<ArrayList<Category>>()
    var productTagList = MutableLiveData<ArrayList<ProductTagCrossRef>>()
    var tagList = MutableLiveData<ArrayList<Tag>>()
    var storeList = MutableLiveData<ArrayList<Store>>()

    init {
        storeSort.value = defaultStoreSort
        receiptWithStoreSort.value = defaultReceiptWithStoreSort
        richProductSort.value = defaultRichProductSort
        loadDataByStoreFilter()
        loadDataByReceiptFilter()
        loadDataByProductFilter()
        loadDataByCategoryFilter()
        loadDataByTagFilter()
    }

    fun clearData() {
        filterCategoryList.postValue(FilterCategoryList())
        filterStoreList.postValue(FilterStoreList())
        filterProductList.postValue(FilterProductList())
        filterReceiptList.postValue(FilterReceiptList())
        productRichList.value?.clear()
        receiptList.value?.clear()
        categoryList.value?.clear()
        storeList.value?.clear()
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

    fun loadDataByTagFilter() {
        filterTagList.value?.let {
            refreshTagList(it.tagName)
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
        viewModelScope.launch {
            receiptList.postValue(
                roomDatabaseHelper.getReceiptWithStoreOrdered(
                    name,
                    dateFrom,
                    dateTo,
                    receiptWithStoreSort.value ?: defaultReceiptWithStoreSort
                ) as ArrayList<ReceiptWithStore>?
            )
        }
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    private fun refreshCategoryList(categoryName: String) {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories(categoryName) as ArrayList<Category>)
        }
    }

    fun refreshTagList() { //TODO wywolanie w hookUpFragment
        viewModelScope.launch {
            tagList.postValue(roomDatabaseHelper.getAllTags() as ArrayList<Tag>)
        }
    }

    private fun refreshTagList(tagName: String) {
        viewModelScope.launch {
            tagList.postValue(roomDatabaseHelper.getAllTags(tagName) as ArrayList<Tag>)
        }
    }

    fun refreshProductTagList() {
        viewModelScope.launch {
            productTagList.postValue(roomDatabaseHelper.getAllProductTags() as ArrayList<ProductTagCrossRef>)
        }
    }

    private fun refreshProductTagList(productId: String) {
        viewModelScope.launch {
            productTagList.postValue(roomDatabaseHelper.getAllProductTags(productId) as ArrayList<ProductTagCrossRef>)
        }
    }

    fun refreshStoreList() {
        viewModelScope.launch {
            storeList.postValue(roomDatabaseHelper.getAllStores() as ArrayList<Store>)
        }
    }

    private fun refreshStoreList(name: String, nip: String) {
        viewModelScope.launch {
            storeList.postValue(
                roomDatabaseHelper.getAllStoresOrdered(
                    name,
                    nip,
                    storeSort.value ?: defaultStoreSort
                ) as ArrayList<Store>
            )
        }
    }

    private fun refreshProductList(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Int,
        higherPrice: Int
    ) {
        viewModelScope.launch {
            productRichList.postValue(
                roomDatabaseHelper.getAllProductsOrderedWithHigherPrice(
                    storeName,
                    categoryName,
                    dateFrom,
                    dateTo,
                    lowerPrice,
                    higherPrice,
                    richProductSort.value ?: defaultRichProductSort
                ) as ArrayList<ProductRichData>
            )
        }
    }
}
