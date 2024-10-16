package com.example.navsample.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ReceiptImageViewModel : ViewModel() {
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
//        clearDirectoryFromTempImages()
//        if (::uri.isInitialized) {
//            uri.value?.let { File(it.path.toString()).delete() }
//        }
//        if (::uriCropped.isInitialized) {
//            uriCropped.value?.let { File(it.path.toString()).delete() }
//        }

        uid = MutableLiveData<String>(null)
        uri = MutableLiveData<Uri?>(null)
        bitmapOriginal = MutableLiveData<Bitmap?>(null)
        bitmapCroppedReceipt = MutableLiveData<Bitmap?>(null)
        uriCroppedProduct = MutableLiveData<Uri?>(null)
        bitmapCroppedProduct = MutableLiveData<Bitmap?>(null)
    }

    private fun clearDirectoryFromTempImages() {
        val paths = mutableListOf<String>()
        val tempFile = File.createTempFile(uid.value ?: "temp", ".png")
        tempFile.parent?.let { parentPath ->
            File(parentPath).listFiles()?.forEach { file ->
                if (file.path.endsWith(".png")) {
                    paths.add(file.path)
                }
            }
        }
        paths.forEach { path ->
            File(path).delete()
        }
    }

    fun setImageUriOriginal() {
        uri.value?.let { File(it.path.toString()).delete() }
        uri.value = bitmapCroppedReceipt.value?.let { setImageUri(it) }
    }

    fun setImageUriCropped() {
        uriCroppedProduct.value?.let { File(it.path.toString()).delete() }
        uriCroppedProduct.value = bitmapCroppedProduct.value?.let { setImageUri(it) }
    }

    private fun setImageUri(bitmap: Bitmap): Uri {

        val tempFile = File.createTempFile(uid.value ?: "temp", ".png")
//        File.
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
