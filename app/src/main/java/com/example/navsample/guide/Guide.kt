package com.example.navsample.guide

import android.content.Context
import android.graphics.BitmapFactory
import com.example.navsample.guide.dialog.GuideDialog
import com.github.chrisbanes.photoview.PhotoView

interface Guide {

    var iterator: Int
    var instructions: List<() -> Unit>
    fun prepare()

    fun previous() {
        iterator -= 1
        doStuff()
    }

    fun next() {
        iterator += 1
        doStuff()
    }

    fun configureDialog(): GuideDialog {
        val guideDialog = GuideDialog(
            "test",
            { previous() },
            { next() }
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


    fun doStuff() {

        if (iterator >= instructions.size) {
            instructions.last().invoke()
        }
        if (iterator < 0) {
            instructions.first().invoke()
        } else {
            instructions[iterator].invoke()
        }
    }
}