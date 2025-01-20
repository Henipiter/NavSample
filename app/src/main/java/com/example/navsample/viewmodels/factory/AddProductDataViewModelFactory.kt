package com.example.navsample.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.navsample.dto.StringProvider
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel

class AddProductDataViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductDataViewModel::class.java)) {
            val dao = ReceiptDatabase.getInstance(application).receiptDao
            val stringProvider = StringProvider(application)
            return AddProductDataViewModel(application, dao, stringProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
