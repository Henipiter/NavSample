package com.example.navsample.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    lateinit var uri: MutableLiveData<Uri?>
    lateinit var bitmapOriginal: MutableLiveData<Bitmap?>
    lateinit var bitmapCroppedReceipt: MutableLiveData<Bitmap?>
    lateinit var uriCroppedProduct: MutableLiveData<Uri?>
    lateinit var bitmapCroppedProduct: MutableLiveData<Bitmap?>
    lateinit var uid: MutableLiveData<String>


    init {
        clearData()
    }

    fun clearData() {
        uid = MutableLiveData<String>(null)
        uri = MutableLiveData<Uri?>(null)
        bitmapOriginal = MutableLiveData<Bitmap?>(null)
        bitmapCroppedReceipt = MutableLiveData<Bitmap?>(null)
        uriCroppedProduct = MutableLiveData<Uri?>(null)
        bitmapCroppedProduct = MutableLiveData<Bitmap?>(null)
    }

}
