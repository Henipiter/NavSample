package com.example.navsample.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.navsample.R
import com.example.navsample.viewmodels.ReceiptDataViewModel

class GuideActivity : AppCompatActivity() {
    companion object {
        private const val FILLED_DB = "filled_db"
    }

    private lateinit var myPref: SharedPreferences
    private val receiptDataViewModel: ReceiptDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myPref = applicationContext.getSharedPreferences(
            "preferences",
            AppCompatActivity.MODE_PRIVATE
        )

        setContentView(R.layout.activity_guide_main)
    }
}