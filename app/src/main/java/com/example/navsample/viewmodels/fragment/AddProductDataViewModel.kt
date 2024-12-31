package com.example.navsample.viewmodels.fragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class AddProductDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var productIndex = -1
    var productId = ""
    var receiptId = ""
    var storeId = ""
    var categoryId = ""

    var chips = MutableLiveData<List<Chip>>()
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

    fun refreshTagsList(context: Context) {
        if (productId.isEmpty()) {
            return
        }
        viewModelScope.launch {
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
            chips.postValue(createChips(context, selectedTags, notSelectedTags))


        }
    }

    private fun createChips(
        context: Context, selectedTags: ArrayList<Tag>, notSelectedTags: ArrayList<Tag>
    ): MutableList<Chip> {
        val chips = mutableListOf<Chip>()
        selectedTags.forEach {
            val chip = Chip(context).apply {
                text = it.name
                isCheckable = true
            }
            chips.add(chip)
        }
        notSelectedTags.forEach {
            val chip = Chip(context).apply {
                text = it.name
                isCheckable = false
            }
            chips.add(chip)
        }
        return chips
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val deletedProduct = roomDatabaseHelper.deleteProductById(productId)
            FirestoreHelperSingleton.getInstance().delete(deletedProduct) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
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

    fun getProductsByReceiptId(receiptId: String) {
        viewModelScope.launch {
            databaseProductList.postValue(roomDatabaseHelper.getProductsByReceiptId(receiptId) as ArrayList)
        }
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            val updatedProduct = roomDatabaseHelper.updateProduct(product)
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
            FirestoreHelperSingleton.getInstance().addFirestore(savedProduct) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProductFirestoreId(savedProduct.id, it)
                }
            }
        }
    }
}
