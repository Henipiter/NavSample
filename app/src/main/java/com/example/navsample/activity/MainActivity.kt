package com.example.navsample.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.navsample.R
import com.example.navsample.chart.ChartColors.Companion.COLORS
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store
import com.example.navsample.viewmodels.ReceiptDataViewModel


class MainActivity : AppCompatActivity() {

    companion object {
        private const val FILLED_DB = "filled_db"
    }

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
        receiptDataViewModel.insertStore(Store("9370008168", "Carrefour", 2))
        receiptDataViewModel.insertStore(Store("7791011327", "Biedronka", 2))
        receiptDataViewModel.insertStore(Store("7811897358", "LIDL", 2))
        receiptDataViewModel.insertStore(Store("8992367273", "KAUFLAND", 2))
        receiptDataViewModel.insertStore(Store("8521021463", "NETTO", 2))
        receiptDataViewModel.insertStore(Store("5562125117", "POLOMARKET", 2))
        receiptDataViewModel.insertStore(Store("1070002973", "ALDI", 2))
        receiptDataViewModel.insertStore(Store("5260309174", "AUCHAN", 2))
        receiptDataViewModel.insertStore(Store("5291799498", "ŻABKA", 2))
        receiptDataViewModel.insertStore(Store("7821977018", "INTERMARCHE", 2))
        receiptDataViewModel.insertStore(Store("6211766191", "DINO", 2))


        receiptDataViewModel.insertStore(Store("5252175977", "SUPER-PHARM", 5))
        receiptDataViewModel.insertStore(Store("7270019183", "ROSSMANN", 5))
        receiptDataViewModel.insertStore(Store("2090001776", "HEBE", 5))


        receiptDataViewModel.insertStore(Store("0001112223", "MARTES-SPORT", 6))


        receiptDataViewModel.insertStore(Store("6222797516", "ZAHIR KEBAB", 1))

        receiptDataViewModel.insertStore(Store("7740001454", "ORLEN", 10))
        receiptDataViewModel.insertStore(Store("7790001083", "CIRCLE K", 10))
        receiptDataViewModel.insertStore(Store("9452127910", "BP", 10))
        receiptDataViewModel.insertStore(Store("5261009190", "SHELL", 10))
        receiptDataViewModel.insertStore(Store("5270011878", "MOYA", 10))
        receiptDataViewModel.insertStore(Store("5861988228", "MOL", 10))


        receiptDataViewModel.insertCategoryList(Category("INNE", COLORS[0]))
        receiptDataViewModel.insertCategoryList(Category("JEDZENIE", COLORS[1]))
        receiptDataViewModel.insertCategoryList(Category("ZDROWIE", COLORS[2]))
        receiptDataViewModel.insertCategoryList(Category("KULTURA", COLORS[3]))
        receiptDataViewModel.insertCategoryList(Category("OPŁATY", COLORS[4]))
        receiptDataViewModel.insertCategoryList(Category("KOSTMETYKI", COLORS[5]))
        receiptDataViewModel.insertCategoryList(Category("SPORT", COLORS[6]))
        receiptDataViewModel.insertCategoryList(Category("MOTORYZACJA", COLORS[7]))
        receiptDataViewModel.insertCategoryList(Category("UBRANIA", COLORS[8]))
        receiptDataViewModel.insertCategoryList(Category("PALIWO", COLORS[9]))
        receiptDataViewModel.insertReceipt(Receipt(1, 7.79, 0.0, "2024-09-22", "17:33"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(1, "D_JZN_ZIEMNIAKI JAD", 1, 0.496, 3.99, 1.98, 0.0, 1.98, "D", ""),
                Product(1, "D_WAFLE_SONKO 130G", 1, 1.0, 3.59, 3.59, 0.0, 3.59, "D", ""),
                Product(1, "D_KAJZERKA 50G", 1, 6.0, 0.37, 2.22, 0.0, 2.22, "D", "")
            )
        )


        receiptDataViewModel.insertReceipt(Receipt(1, 28.11, 0.0, "2024-09-21", "18:30"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(2, "D_KAJZERKA 50G", 1, 8.0, 0.37, 2.96, 0.0, 2.96, "D", ""),
                Product(2, "D_SEREK_ALMETTE 150", 1, 1.0, 4.99, 4.99, 0.0, 4.99, "D", ""),
                Product(2, "D_MC SALADA LODOWA", 1, 1.0, 4.99, 4.99, 0.0, 4.99, "D", ""),
                Product(2, "D_RZODKIEWKA PECZEK", 1, 1.0, 2.29, 2.29, 0.0, 2.29, "D", ""),
                Product(2, "D_SER MAGNAT", 1, 0.204, 39.90, 8.14, 0.0, 8.14, "D", ""),
                Product(2, "D_GREJPFRUTY BIALE L", 1, 0.348, 7.69, 2.68, 0.0, 2.68, "D", ""),
                Product(2, "D_MC CYTRYNY LUZ", 1, 0.258, 7.99, 2.06, 0.0, 2.06, "D", ""),
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(2, 60.17, 7.69, "2024-09-22", "14:17"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(3, "ChlebWiel400g 50G", 1, 1.0, 3.59, 3.59, 0.0, 3.59, "D", ""),
                Product(3, "Papier Queen 10r 150", 1, 1.0, 4.99, 4.99, 0.0, 4.99, "D", ""),
                Product(3, "Chust Delikat x150", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", ""),
                Product(3, "Chust Delikat x150", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", ""),
                Product(3, "Chust Delikat 10x10", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", ""),
                Product(3, "MakaronRyzAsia200g", 1, 1.0, 5.99, 5.99, 0.0, 5.99, "D", ""),
                Product(3, "SmootZOwocLes0.75", 1, 1.0, 6.66, 6.66, 0.0, 6.66, "D", ""),
                Product(3, "Marchew luz", 1, 0.335, 2.99, 1.0, 0.0, 1.0, "D", ""),
                Product(3, "JablPolskieGalaLuz", 1, 0.395, 3.49, 1.38, 0.0, 1.38, "D", ""),
                Product(3, "CebulaZolta Luz", 1, 0.08, 4.99, 0.4, 0.0, 0.4, "D", "")
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(4, 12.24, 0.01, "2024-09-18", "19:54"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(
                    4,
                    "IBUPROM 200MG 10 TABL POWL 8%",
                    2,
                    1.0,
                    8.24,
                    3.59,
                    0.0,
                    3.59,
                    "D",
                    ""
                ),
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(5, 8.24, 0.59, "2024-08-18", "19:54"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(5, "KAPIELOWKI", 7, 1.0, 89.99, 89.99, 0.0, 89.99, "D", ""),
                Product(5, "BUTY BIEGANIE", 7, 1.0, 299.99, 299.99, 0.0, 299.99, "D", ""),
            )
        )
        receiptDataViewModel.insertReceipt(Receipt(5, 98.24, 0.61, "2024-07-18", "19:54"))
        receiptDataViewModel.insertProducts(
            listOf(
                Product(6, "KAPIELOWKI", 7, 1.0, 89.99, 89.99, 0.0, 89.99, "D", ""),
                Product(6, "BUTY BIEGANIE", 7, 1.0, 299.99, 299.99, 0.0, 299.99, "D", ""),
            )
        )
    }

}