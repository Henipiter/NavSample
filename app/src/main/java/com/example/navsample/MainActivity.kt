package com.example.navsample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
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
        receiptDataViewModel.insertStore(Store("SUPER-PHARM", "5252175977"))

        receiptDataViewModel.insertCategoryList(Category("INNE"))
        receiptDataViewModel.insertCategoryList(Category("JEDZENIE"))
        receiptDataViewModel.insertCategoryList(Category("ZDROWIE"))
        receiptDataViewModel.insertCategoryList(Category("KULTURA"))
        receiptDataViewModel.insertCategoryList(Category("OPŁATY"))
        receiptDataViewModel.insertCategoryList(Category("KOSTMETYKI"))
        receiptDataViewModel.insertCategoryList(Category("SPORT"))
        receiptDataViewModel.insertCategoryList(Category("MOTORYZACJA"))
        receiptDataViewModel.insertCategoryList(Category("SPORT"))
        receiptDataViewModel.insertCategoryList(Category("UBRANIA"))
        receiptDataViewModel.insertReceipt(Receipt(1, 7.79F, 0F, "2023-09-22", "17:33"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(1, "D_JZN_ZIEMNIAKI JAD", 1, 0.496F, 3.99F, 1.98F, "D", ""),
                Product(1, "D_WAFLE_SONKO 130G", 1, 1F, 3.59F, 3.59F, "D", ""),
                Product(1, "D_KAJZERKA 50G", 1, 6F, 0.37F, 2.22F, "D", "")
            )
        )


        receiptDataViewModel.insertReceipt(Receipt(1, 28.11F, 0F, "2023-09-21", "18:30"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(2, "D_KAJZERKA 50G", 1, 8F, 0.37F, 2.96F, "D", ""),
                Product(2, "D_SEREK_ALMETTE 150", 1, 1F, 4.99F, 4.99F, "D", ""),
                Product(2, "D_MC SALADA LODOWA", 1, 1F, 4.99F, 4.99F, "D", ""),
                Product(2, "D_RZODKIEWKA PECZEK", 1, 1F, 2.29F, 2.29F, "D", ""),
                Product(2, "D_SER MAGNAT", 1, 0.204F, 39.90F, 8.14F, "D", ""),
                Product(2, "D_GREJPFRUTY BIALE L", 1, 0.348F, 7.69F, 2.68F, "D", ""),
                Product(2, "D_MC CYTRYNY LUZ", 1, 0.258F, 7.99F, 2.06F, "D", ""),
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(2, 60.17F, 7.69F, "2023-09-22", "14:17"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(3, "ChlebWiel400g 50G", 1, 1F, 3.59F, 3.59F, "D", ""),
                Product(3, "Papier Queen 10r 150", 1, 1F, 4.99F, 4.99F, "D", ""),
                Product(3, "Chust Delikat x150", 5, 1F, 4.39F, 4.39F, "A", ""),
                Product(3, "Chust Delikat x150", 5, 1F, 4.39F, 4.39F, "A", ""),
                Product(3, "Chust Delikat 10x10", 5, 1F, 4.39F, 4.39F, "A", ""),
                Product(3, "MakaronRyzAsia200g", 1, 1F, 5.99F, 5.99F, "D", ""),
                Product(3, "SmootZOwocLes0,75", 1, 1F, 6.666F, 6.66F, "D", ""),
                Product(3, "Marchew luz", 1, 0.335F, 2.99F, 1.0F, "D", ""),
                Product(3, "JablPolskieGalaLuz", 1, 0.395F, 3.49F, 1.38F, "D", ""),
                Product(3, "CebulaZolta Luz", 1, 0.08F, 4.99F, 0.4F, "D", "")
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(4, 8.24F, 0.61F, "2023-09-18", "19:54"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(4, "IBUPROM 200MG 10 TABL POWL 8%", 2, 1F, 8.24F, 3.59F, "D", ""),
            )
        )
    }

}