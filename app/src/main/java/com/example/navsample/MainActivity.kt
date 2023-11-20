package com.example.navsample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.navsample.entities.Category
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ReceiptDataViewModel


class MainActivity : AppCompatActivity() {


    private val FILLED_DB = "filled_db"
    private lateinit var myPref: SharedPreferences
    private val receiptDataViewModel: ReceiptDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myPref = applicationContext.getSharedPreferences("preferences", MODE_PRIVATE)
        if (myPref.getString(FILLED_DB, "false") == "false") {
            initDatabase()
            myPref.edit().putString(FILLED_DB, "true").apply()
        }
        setContentView(R.layout.activity_main)
    }

    private fun initDatabase() {
        receiptDataViewModel.insertStore(Store("Carrefour", "9370008168"))
        receiptDataViewModel.insertStore(Store("Biedronka", "7791011327"))
        receiptDataViewModel.insertStore(Store("LIDL", "7811897358"))

        receiptDataViewModel.insertCategoryList(Category("INNE"))
        receiptDataViewModel.insertCategoryList(Category("JEDZENIE"))
        receiptDataViewModel.insertCategoryList(Category("ZDROWIE"))
        receiptDataViewModel.insertCategoryList(Category("KULTURA"))
        receiptDataViewModel.insertCategoryList(Category("OPŁATY"))
        receiptDataViewModel.insertCategoryList(Category("KOSTMETYKI"))
        receiptDataViewModel.insertCategoryList(Category("SPORT"))
        receiptDataViewModel.insertCategoryList(Category("MOTORYZACJA"))
        receiptDataViewModel.insertCategoryList(Category("SPORT"))

    }

}