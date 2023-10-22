package com.example.navsample.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ReceiptImageViewModel : ViewModel() {
    var uri = MutableLiveData<Uri?>(null)
    var bitmap = MutableLiveData<Bitmap?>(null)
    var uriCropped = MutableLiveData<Uri?>(null)
    var bitmapCropped = MutableLiveData<Bitmap?>(null)


    fun setImageUriOriginal() {
        uri.value = bitmap.value?.let { setImageUri(it) }
    }
    fun setImageUriCropped() {
        uriCropped.value = bitmapCropped.value?.let { setImageUri(it) }
    }

    private fun setImageUri(bitmap: Bitmap): Uri {

        val tempFile = File.createTempFile("temp", ".png")
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)

    }

}
