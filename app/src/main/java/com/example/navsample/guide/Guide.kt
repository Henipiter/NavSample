package com.example.navsample.guide

import android.content.Context
import android.graphics.BitmapFactory
import com.example.navsample.guide.dialog.GuideDialog
import com.github.chrisbanes.photoview.PhotoView
import java.lang.Integer.max
import java.lang.Integer.min

interface Guide {

    var iterator: Int
    var instructions: List<() -> Unit>
    fun prepare()

    fun previous() {
        iterator = max(iterator - 1, 0)
        doStuff()
    }

    fun next() {
        iterator = min(iterator + 1, instructions.size - 1)
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