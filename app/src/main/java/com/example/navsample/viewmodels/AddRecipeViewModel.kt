package com.example.navsample.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddRecipeViewModel : ViewModel() {
    var imageUri = MutableLiveData<Uri?>(null)
}