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
import com.example.navsample.entities.RoomDatabaseHelper
import kotlinx.coroutines.launch

class AddCategoryDataViewModel : ViewModel() {

    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
    private lateinit var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper = RoomDatabaseHelper(dao!!)

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
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            roomDatabaseHelper.deleteCategory(category)
        }
    }

    fun getCategoryById(id: String) {
        viewModelScope.launch {
            categoryById.postValue(roomDatabaseHelper.getCategoryById(id))
        }
    }

    fun insertCategory(newCategory: Category) {
        Log.i("Database", "insert category: ${newCategory.name}")
        viewModelScope.launch {
            savedCategory.postValue(roomDatabaseHelper.insertCategory(newCategory))
            firebaseHelper.addFirestore(savedCategory.value!!)
        }
    }


    fun updateCategory(newCategory: Category) {
        viewModelScope.launch {
            roomDatabaseHelper.updateCategory(newCategory)
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
