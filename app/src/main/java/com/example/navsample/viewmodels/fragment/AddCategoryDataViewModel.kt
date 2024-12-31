package com.example.navsample.viewmodels.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import kotlinx.coroutines.launch

class AddCategoryDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var categoryId = ""

    var categoryList = MutableLiveData<ArrayList<Category>>()
    var categoryById = MutableLiveData<Category?>()
    var savedCategory = MutableLiveData<Category>()

    init {

        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val deletedCategory = roomDatabaseHelper.deleteTag(categoryId)
            FirestoreHelperSingleton.getInstance().delete(deletedCategory) { id ->
                viewModelScope.launch { roomDatabaseHelper.markCategoryAsDeleted(id) }
            }
        }
    }

    fun getCategoryById(id: String) {
        viewModelScope.launch {
            categoryById.postValue(roomDatabaseHelper.getCategoryById(id))
        }
    }

    fun insertCategory(newCategory: Category, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedCategory = roomDatabaseHelper.insertCategory(newCategory, generateId)
            savedCategory.postValue(insertedCategory)
            FirestoreHelperSingleton.getInstance().addFirestore(insertedCategory) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateCategoryFirestoreId(insertedCategory.id, it)
                }
            }
        }
    }

    fun updateCategory(newCategory: Category) {
        viewModelScope.launch {
            val updatedCategory = roomDatabaseHelper.updateCategory(newCategory)
            savedCategory.postValue(updatedCategory)
            if (newCategory.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updatedCategory) {
                    viewModelScope.launch { roomDatabaseHelper.markCategoryAsUpdated(updatedCategory.id) }
                }
            }
        }
    }


}
