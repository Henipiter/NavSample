package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.ReceiptDatabase
import kotlinx.coroutines.launch
import java.util.UUID

class AddCategoryDataViewModel : ViewModel() {

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper

    var inputType = AddingInputType.EMPTY.name
    var categoryId = ""

    var categoryList = MutableLiveData<ArrayList<Category>>()
    var categoryById = MutableLiveData<Category?>()
    var savedCategory = MutableLiveData<Category>()
    private var userUuid = MutableLiveData<String?>(null)

    init {
        setUserUuid()
    }

    fun refreshCategoryList() {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryList.postValue(
                dao?.getAllCategories() as ArrayList<Category>
            )
        }
    }


    fun deleteCategory(category: Category) {
        Log.i("Database", "delete category - id ${category.id}")
        viewModelScope.launch {
            dao?.deleteCategory(category)
        }
    }

    fun getCategoryById(id: String) {
        Log.i("Database", "refresh category list")
        viewModelScope.launch {
            categoryById.postValue(
                dao?.getCategoryById(id)
            )
        }
    }

    fun insertCategory(newCategory: Category) {
        Log.i("Database", "insert category: ${newCategory.name}")
        viewModelScope.launch {
            newCategory.id = UUID.randomUUID().toString()
            dao?.let {
                dao.insertCategory(newCategory)
            }
            Log.i("Database", "inserted category with id ${newCategory.id}")
            savedCategory.value = newCategory
            firebaseHelper.addFirestore(newCategory)
        }
    }


    fun updateCategory(newCategory: Category) {
        Log.i("Database", "update category with id ${newCategory.id}: ${newCategory.name}")
        viewModelScope.launch {
            dao?.let {
                dao.updateCategory(newCategory)
            }
            savedCategory.postValue(newCategory)
            firebaseHelper.updateFirestore(newCategory)
        }
    }


    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
                firebaseHelper = FirebaseHelper(uuid!!)
            }
        }
    }


}
