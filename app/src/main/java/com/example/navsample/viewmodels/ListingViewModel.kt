package com.example.navsample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.relations.GroupedProductWithTag
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ProductWithTag
import com.example.navsample.entities.relations.ReceiptWithStore
import kotlinx.coroutines.launch

class ListingViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val dao = ReceiptDatabase.getInstance(application).receiptDao
    private var roomDatabaseHelper = RoomDatabaseHelper(dao)

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
    var productTagList = MutableLiveData<ArrayList<GroupedProductWithTag>>()
    var tagList = MutableLiveData<ArrayList<Tag>>()
    var storeList = MutableLiveData<ArrayList<Store>>()

    init {
        storeSort.value = defaultStoreSort
        receiptWithStoreSort.value = defaultReceiptWithStoreSort
        richProductSort.value = defaultRichProductSort
        loadDataByStoreFilter()
        loadDataByReceiptFilter()
        loadDataByProductFilter()
        refreshProductTagList()
        loadDataByCategoryFilter()
        loadDataByTagFilter()
    }

    fun clearData() {
        filterCategoryList.postValue(FilterCategoryList())
        filterStoreList.postValue(FilterStoreList())
        filterReceiptList.postValue(FilterReceiptList())
        filterProductList.postValue(FilterProductList())
        filterTagList.postValue(FilterTagList())
        categoryList.value?.clear()
        storeList.value?.clear()
        receiptList.value?.clear()
        productRichList.value?.clear()
        tagList.value?.clear()
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
            val list = roomDatabaseHelper.getProductWithTag()
            val ids = list?.map { it.id }
            val groupedProductWithTags = arrayListOf<GroupedProductWithTag>()
            ids?.forEach {
                groupedProductWithTags.add(GroupedProductWithTag.convert(it, list))
            }
            productTagList.postValue(groupedProductWithTags)
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


            val productTagList = roomDatabaseHelper.getProductWithTag()
            val productList = roomDatabaseHelper.getAllProductsOrderedWithHigherPrice(
                storeName,
                categoryName,
                dateFrom,
                dateTo,
                lowerPrice,
                higherPrice,
                richProductSort.value ?: defaultRichProductSort
            ) as ArrayList<ProductRichData>

            getTagsForAllProducts(productList, productTagList)


            productRichList.postValue(productList)
        }
    }

    private fun getTagsForAllProducts(
        productList: List<ProductRichData>, productTags: List<ProductWithTag>?
    ) {
        productList.forEach {
            val tagsList = getTagsForProductId(it.id, productTags)
            it.tagList = tagsList
        }
    }

    private fun getTagsForProductId(
        productId: String,
        productTags: List<ProductWithTag>?
    ): ArrayList<Tag> {
        val tags = arrayListOf<Tag>()
        productTags?.forEach {
            if (it.deletedAt.isEmpty() && it.id == productId && it.tagId != null) {
                val tag = Tag(it.tagName)
                tag.id = it.tagId!!
                tags.add(tag)
            }
        }
        return tags
    }

}
