package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navsample.DTO.Product
import com.example.navsample.DTO.Receipt

class ReceiptDataViewModel : ViewModel() {
    var receipt = MutableLiveData<Receipt?>(null)
    var product = MutableLiveData<ArrayList<Product>>(ArrayList())



}
