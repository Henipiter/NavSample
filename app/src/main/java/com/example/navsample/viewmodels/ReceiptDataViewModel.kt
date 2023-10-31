package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO

class ReceiptDataViewModel : ViewModel() {
    var receipt = MutableLiveData<ReceiptDTO?>(null)
    var product = MutableLiveData<ArrayList<ProductDTO>>(ArrayList())



}
