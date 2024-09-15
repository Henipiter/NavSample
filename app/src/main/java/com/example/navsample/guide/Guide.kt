package com.example.navsample.guide

import android.content.Context
import android.graphics.BitmapFactory
import com.example.navsample.guide.dialog.GuideDialog
import com.github.chrisbanes.photoview.PhotoView

interface Guide {

    var iterator: Int
    var texts: List<String>
    var instructions: List<() -> Unit>
    fun prepare()
    fun configureDialog(): GuideDialog {
        val guideDialog = GuideDialog(
            iterator, texts, instructions
        )
        guideDialog.isCancelable = false
        return guideDialog
    }

    fun getPhotoView(): PhotoView

    fun loadImage(imageName: String)
    fun loadImage(imageName: String, context: Context) {
        val inputStream = context.assets.open("guide/$imageName")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        getPhotoView().setImageBitmap(bitmap)
    }


}