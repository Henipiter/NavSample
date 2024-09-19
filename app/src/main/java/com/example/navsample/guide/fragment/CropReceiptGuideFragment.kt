package com.example.navsample.guide.fragment

import androidx.navigation.Navigation
import com.example.navsample.R

class CropReceiptGuideFragment : ScreenshotGuideFragment() {
    private val imagesToShow = arrayListOf(
        "original_receipt_cropping.jpg",
        "original_receipt_cropping_2.jpg"
    )

    override fun prepare() {
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage(imagesToShow[0]) },
            { loadImage(imagesToShow[1]) },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_cropReceiptGuideFragment_to_addReceiptGuideFragment)
            }
        )
        texts = listOf(
            "Have to cut",
            "Cut to receipt",
            ""
        )

        verticalLevel = listOf(
            100, 100, 100
        )
    }

}