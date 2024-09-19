package com.example.navsample.guide.fragment

import androidx.navigation.Navigation
import com.example.navsample.R

class CropProductGuideFragment : ScreenshotGuideFragment() {
    private val imagesToShow = arrayListOf(
        "crop_receipt_cropping.jpg"
    )

    override fun prepare() {
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage(imagesToShow[0]) },
            {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_cropProductGuideFragment_to_experimentalListGuideFragment)
            }
        )
        texts = listOf(
            "Cut to products",
            ""
        )

        verticalLevel = listOf(
            100, 100, 100, 100
        )
    }
}