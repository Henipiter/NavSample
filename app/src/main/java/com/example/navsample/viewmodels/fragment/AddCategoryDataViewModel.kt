package com.example.navsample.viewmodels.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.TranslateEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AddCategoryDataViewModel : ViewModel() {

    companion object {
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }

    private val firestore = Firebase.firestore
    private val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }

    var categoryList = MutableLiveData<ArrayList<Category>>()
    var categoryById = MutableLiveData<Category?>()
    var savedCategory = MutableLiveData<Category>()
    var userUuid = MutableLiveData<String?>(null)

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

    fun getCategoryById(id: Int) {
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
            dao?.let {
                val rowId = dao.insertCategory(newCategory)
                newCategory.id = dao.getCategoryId(rowId)
            }
            Log.i("Database", "inserted category with id ${newCategory.id}")
            savedCategory.value = newCategory
            addFirestore(newCategory)
        }
    }


    fun updateCategory(newCategory: Category) {
        Log.i("Database", "update category with id ${newCategory.id}: ${newCategory.name}")
        viewModelScope.launch {
            dao?.let {
                dao.updateCategory(newCategory)
            }
            savedCategory.postValue(newCategory)
            updateFirestore(newCategory)
        }
    }

    private fun <T : TranslateEntity> addFirestore(obj: T) {
        getFirestoreUserPath().document(obj.getDescriptiveId()).set(obj)
    }

    private fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath()
            .document(obj.getDescriptiveId())
            .update(obj.toMap())

    }

    private fun getFirestoreUserPath(): CollectionReference {
        return firestore.collection("user").document(userUuid.value.toString())
            .collection(CATEGORY_FIRESTORE_PATH)

    }

    private fun setUserUuid() {
        viewModelScope.launch {
            dao?.let {
                val uuid = dao.getUserUuid()
                userUuid.postValue(uuid)
            }
        }
    }


}
