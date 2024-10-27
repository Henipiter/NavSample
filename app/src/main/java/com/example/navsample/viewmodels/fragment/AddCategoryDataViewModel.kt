package com.example.navsample.viewmodels.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import kotlinx.coroutines.launch

class AddCategoryDataViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var categoryId = ""

    var categoryList = MutableLiveData<ArrayList<Category>>()
    var categoryById = MutableLiveData<Category?>()
    var savedCategory = MutableLiveData<Category>()

    init {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        val userUuid = myPref?.getString("userId", null) ?: throw Exception("NOT SET userId")
        firebaseHelper = FirebaseHelper(userUuid)

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
            val deletedCategory = roomDatabaseHelper.deleteCategory(categoryId)
            firebaseHelper.delete(deletedCategory)
        }
    }

    fun getCategoryById(id: String) {
        viewModelScope.launch {
            categoryById.postValue(roomDatabaseHelper.getCategoryById(id))
        }
    }

    fun insertCategory(newCategory: Category) {
        viewModelScope.launch {
            val insertedCategory = roomDatabaseHelper.insertCategory(newCategory)
            savedCategory.postValue(insertedCategory)
            firebaseHelper.addFirestore(insertedCategory) {
                insertedCategory.firestoreId = it
                updateCategory(insertedCategory)
                firebaseHelper.updateFirestore(insertedCategory)
            }
        }
    }

    fun updateCategory(newCategory: Category) {
        viewModelScope.launch {
            val updatedCategory = roomDatabaseHelper.updateCategory(newCategory)
            savedCategory.postValue(updatedCategory)
            firebaseHelper.updateFirestore(updatedCategory)
        }
    }


}
