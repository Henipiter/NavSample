package com.example.navsample.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class RecipeImageViewModel : ViewModel() {
    var uri = MutableLiveData<Uri?>(null)
    var bitmap = MutableLiveData<Bitmap?>(null)
    fun setImageUri( ) {
        bitmap.value?.let {
            val tempFile = File.createTempFile("temp", ".png")
            val bytes = ByteArrayOutputStream()
            bitmap.value!!.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val bitmapData = bytes.toByteArray()
            val fileOutPut = FileOutputStream(tempFile)
            fileOutPut.write(bitmapData)
            fileOutPut.flush()
            fileOutPut.close()
            uri.value = Uri.fromFile(tempFile)
        }
    }

}