package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.TagList
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.relations.ProductWithTag
import kotlinx.coroutines.launch

class AddProductDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var productIndex = -1
    var productId = ""
    var receiptId = ""
    var storeId = ""
    var categoryId = ""

    var tagList = MutableLiveData<TagList>()
    var categoryList = MutableLiveData<List<Category>>()
    var databaseProductList = MutableLiveData<ArrayList<Product>>()
    var temporaryProductList = MutableLiveData<ArrayList<Product>>()
    var aggregatedProductList = MutableLiveData<ArrayList<Product>>()
    var receiptById = MutableLiveData<Receipt?>()
    var productById = MutableLiveData<Product?>()
    var storeById = MutableLiveData<Store?>()
    var cropImageFragmentOnStart = true

    init {
        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun aggregateProductList(): List<Product> {
        val aggregatedList = arrayListOf<Product>()
        databaseProductList.value?.let { aggregatedList.addAll(it) }
        temporaryProductList.value?.let { aggregatedList.addAll(it) }
        aggregatedProductList.postValue(aggregatedList)
        return aggregatedList
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories())
        }
    }


    fun insertProductTags(productTagCrossRef: ProductTagCrossRef) {
        viewModelScope.launch {
            val savedProductTag =
                roomDatabaseHelper.insertProductTag(productTagCrossRef)
            FirestoreHelperSingleton.getInstance().addFirestore(savedProductTag) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProductTagFirestoreId(savedProductTag.id, it)
                }
            }
        }
    }

    fun refreshTagsList() {
        if (productId.isEmpty()) {
            return
        }
        viewModelScope.launch {
            //TODO change handling selected/non-selected; Get it by db query
            val currentTagList = roomDatabaseHelper.getAllTags()
            val productTagIds = roomDatabaseHelper.getAllProductTags(productId).map { it.tagId }

            val selectedTags = arrayListOf<Tag>()
            val notSelectedTags = arrayListOf<Tag>()
            currentTagList.forEach { tag ->
                if (productTagIds.contains(tag.id)) {
                    selectedTags.add(tag)
                } else {
                    notSelectedTags.add(tag)
                }
            }
            tagList.postValue(TagList(selectedTags, notSelectedTags))
        }
    }


    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val deletedProduct = roomDatabaseHelper.deleteProductById(productId)
            FirestoreHelperSingleton.getInstance().delete(deletedProduct) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
        }
    }

    fun deleteProductTags(productId: String, tagId: String) {
        viewModelScope.launch {
            val deletedProductTag = roomDatabaseHelper.deleteProductTag(productId, tagId)
            FirestoreHelperSingleton.getInstance().delete(deletedProductTag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductTagAsDeleted(id) }
            }
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

    fun getProductByReceiptIdWithTags(receiptId: String) {
        viewModelScope.launch {
            val productTagList = roomDatabaseHelper.getProductWithTag()
            val productList = roomDatabaseHelper.getProductsByReceiptId(receiptId) as ArrayList
            getTagsForAllProducts(productList, productTagList)
            databaseProductList.postValue(productList)
        }
    }

    private fun getTagsForAllProducts(
        productList: List<Product>, productTags: List<ProductWithTag>?
    ) {
        productList.forEach {
            val tagsList = getTagsForProductId(it.id, productTags)
            it.tagList = tagsList
            it.originalTagList = tagsList
        }
    }

    private fun getTagsForProductId(
        productId: String,
        productTags: List<ProductWithTag>?
    ): ArrayList<Tag> {
        val tags = arrayListOf<Tag>()
        productTags?.forEach {
            if (it.id == productId && it.tagId != null) {
                val tag = Tag(it.tagName)
                tag.id = it.tagId!!
                tags.add(tag)
            }
        }
        return tags
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            val updatedProduct = roomDatabaseHelper.updateProduct(product)
            changeProductTags(updatedProduct.id, product)
            if (updatedProduct.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updatedProduct) {
                    viewModelScope.launch { roomDatabaseHelper.markProductAsUpdated(product.id) }
                }
            }
        }
    }

    fun insertProducts(products: List<Product>) {
        products.forEach { product ->
            if (product.id.isEmpty()) {
                insertSingleProduct(product)
            } else {
                updateSingleProduct(product)
            }
        }
    }

    private fun insertSingleProduct(product: Product) {
        viewModelScope.launch {
            val savedProduct = roomDatabaseHelper.insertProduct(product)
            addProductTags(savedProduct.id, product)
            FirestoreHelperSingleton.getInstance().addFirestore(savedProduct) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProductFirestoreId(savedProduct.id, it)
                }
            }
        }
    }

    private fun addProductTags(productId: String, product: Product) {
        val idToSave = product.tagList.map { it.id }
        idToSave.forEach {
            insertProductTags(ProductTagCrossRef(productId, it))
        }
    }

    private fun changeProductTags(productId: String, product: Product) {
        val originalTagIds = product.originalTagList.map { it.id }
        val currentTagIds = product.tagList.map { it.id }


        val idToSave = currentTagIds.filter { !originalTagIds.contains(it) }
        Log.d("EEAARR", "toSave $idToSave")
        val idToDelete = originalTagIds.filter { !currentTagIds.contains(it) }
        Log.d("EEAARR", "toDelete $idToDelete")

        idToSave.forEach {
            insertProductTags(ProductTagCrossRef(productId, it))
        }
        idToDelete.forEach {
            deleteProductTags(productId, it)
        }


    }
}
