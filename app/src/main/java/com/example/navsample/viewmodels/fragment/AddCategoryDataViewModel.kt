package com.example.navsample.viewmodels.fragment

import android.app.Application
import android.graphics.Color
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.R
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.inputs.CategoryErrorInputsMessage
import com.example.navsample.entities.inputs.CategoryInputs
import kotlinx.coroutines.launch

class AddCategoryDataViewModel(
    private var application: Application
) : AndroidViewModel(application) {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var categoryId = ""

    var mode = DataMode.NEW

    var categoryList = MutableLiveData<ArrayList<Category>>()
    var categoryById = MutableLiveData<Category?>()
    var savedCategory = MutableLiveData<Category>()

    init {
        val dao = ReceiptDatabase.getInstance(application).receiptDao
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories() as ArrayList<Category>)
        }
    }

    fun deleteCategory(categoryId: String, onFinish: () -> Unit) {
        viewModelScope.launch {
            roomDatabaseHelper.deleteCategory(categoryId)
            onFinish.invoke()
        }
    }

    fun getCategoryById(id: String) {
        viewModelScope.launch {
            categoryById.postValue(roomDatabaseHelper.getCategoryById(id))
        }
    }

    private fun insertCategory(newCategory: Category, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedCategory = roomDatabaseHelper.insertCategory(newCategory, generateId)
            savedCategory.postValue(insertedCategory)
        }
    }

    private fun updateCategory(newCategory: Category) {
        viewModelScope.launch {
            val updatedCategory = roomDatabaseHelper.updateCategory(newCategory)
            savedCategory.postValue(updatedCategory)
        }
    }

    fun saveCategory(inputs: CategoryInputs) {
        if (mode == DataMode.NEW) {
            val category = Category(
                inputs.name.toString(),
                inputs.color.toString()
            )
            insertCategory(category)
        }
        if (mode == DataMode.EDIT) {
            val category = categoryById.value!!
            category.name = inputs.name.toString()
            category.color = inputs.color.toString()
            updateCategory(category)
        }
    }

    fun validateName(text: CharSequence?): String? {
        val currentCategoryName = categoryById.value?.name
        val categoryList = categoryList.value

        return if (text.isNullOrEmpty()) {
            getString(application, R.string.empty_value_error)
        } else if (text.toString() == currentCategoryName) {
            null
        } else if (categoryList?.find { it.name == text.toString() } != null) {
            getString(application, R.string.category_already_exists)
        } else {
            null
        }
    }

    fun validateColor(text: CharSequence?): String? {
        if (text == null || text.length != 7 || text[0] != '#') {
            return getString(application, R.string.invalid_color_format)
        }
        try {
            Color.parseColor(text.toString())
        } catch (e: IllegalArgumentException) {
            return getString(application, R.string.invalid_color_format)
        }
        return null
    }

    fun validateObligatoryFields(categoryInputs: CategoryInputs): CategoryErrorInputsMessage {
        val errors = CategoryErrorInputsMessage()
        errors.name = validateName(categoryInputs.name)
        errors.color = validateColor(categoryInputs.color)
        return errors
    }

}
