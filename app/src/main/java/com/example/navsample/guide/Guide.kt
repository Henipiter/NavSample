package com.example.navsample.guide

import android.content.Context
import android.graphics.BitmapFactory
import com.canhub.cropper.CropImageView
import com.example.navsample.guide.dialog.GuideDialog
import com.github.chrisbanes.photoview.PhotoView

interface Guide {

    var iterator: Int
    var texts: List<String>
    var instructions: List<() -> Unit>
    var verticalLevel: List<Int>
    fun prepare()
    fun configureDialog(): GuideDialog {
        val guideDialog = GuideDialog(
            iterator, texts, instructions, verticalLevel
        )
        guideDialog.isCancelable = false


        return guideDialog
    }

    fun getPhotoView(): PhotoView
    fun getCropImageView(): CropImageView

    fun loadImage(imageName: String)
    fun loadCropImageView(imageName: String)
    fun loadImage(imageName: String, context: Context) {
        val inputStream = context.assets.open("guide/$imageName")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        getPhotoView().setImageBitmap(bitmap)
    }

    fun loadCropImageView(imageName: String, context: Context) {
        val inputStream = context.assets.open("guide/$imageName")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        getCropImageView().setImageBitmap(bitmap)
    }


}