package com.example.navsample.entities.init

import com.example.navsample.chart.ChartColors
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store

class InitDatabaseHelper {


    companion object {
        fun getProducts(): List<List<Product>> {
            return listOf(
                listOf(
                    Product(
                        1,
                        "D_JZN_ZIEMNIAKI JAD",
                        1,
                        496,
                        399,
                        198,
                        0,
                        198,
                        "D",
                        "",
                        true
                    ),
                    Product(1, "D_WAFLE_SONKO 130G", 1, 1000, 359, 359, 0, 359, "D", "", true),
                    Product(1, "D_KAJZERKA 50G", 1, 6000, 37, 222, 0, 222, "D", "", true)
                ),
                listOf(
                    Product(2, "D_KAJZERKA 50G", 1, 8000, 37, 296, 0, 296, "D", "", true),
                    Product(2, "D_SEREK_ALMETTE 150", 1, 1000, 499, 499, 0, 499, "D", "", true),
                    Product(2, "D_MC SALADA LODOWA", 1, 1000, 499, 499, 0, 499, "D", "", true),
                    Product(2, "D_RZODKIEWKA PECZEK", 1, 1000, 229, 229, 0, 229, "D", "", true),
                    Product(2, "D_SER MAGNAT", 1, 204, 3990, 814, 0, 814, "D", "", true),
                    Product(
                        2,
                        "D_GREJPFRUTY BIALE L",
                        1,
                        348,
                        769,
                        268,
                        0,
                        268,
                        "D",
                        "",
                        true
                    ),
                    Product(2, "D_MC CYTRYNY LUZ", 1, 258, 799, 206, 0, 206, "D", "", true)
                ),
                listOf(
                    Product(3, "ChlebWiel400g 50G", 1, 1000, 359, 359, 0, 359, "D", "", true),
                    Product(
                        3,
                        "Papier Queen 10r 150",
                        1,
                        1000,
                        499,
                        499,
                        0,
                        499,
                        "D",
                        "",
                        true
                    ),
                    Product(3, "Chust Delikat x150", 5, 100, 439, 439, 0, 439, "A", "", true),
                    Product(3, "Chust Delikat x150", 5, 100, 439, 439, 0, 439, "A", "", true),
                    Product(3, "Chust Delikat 10x10", 5, 100, 439, 439, 0, 439, "A", "", true),
                    Product(3, "MakaronRyzAsia200g", 1, 100, 599, 599, 0, 599, "D", "", true),
                    Product(3, "SmootZOwocLes075", 1, 100, 666, 666, 0, 666, "D", "", true),
                    Product(3, "Marchew luz", 1, 335, 299, 100, 0, 100, "D", "", true),
                    Product(
                        3,
                        "JablPolskieGalaLuz",
                        1,
                        395,
                        349,
                        138,
                        0,
                        138,
                        "D",
                        "",
                        true
                    ),
                    Product(3, "CebulaZolta Luz", 1, 8, 499, 40, 0, 40, "D", "", true)
                ),
                listOf(
                    Product(
                        4,
                        "IBUPROM 200MG 10 TABL",
                        2,
                        1000,
                        824,
                        359,
                        0,
                        359,
                        "D",
                        "",
                        true
                    )
                ),
                listOf(
                    Product(5, "KAPIELOWKI", 7, 1000, 8999, 8999, 0, 8999, "D", "", true),
                    Product(5, "BUTY BIEGANIE", 7, 1000, 29999, 29999, 0, 29999, "D", "", true)
                ),
                listOf(
                    Product(6, "KAPIELOWKI", 7, 1000, 8999, 8999, 0, 8999, "D", "", true),
                    Product(6, "BUTY BIEGANIE", 7, 100, 29999, 29999, 0, 29999, "D", "", true)
                )
            )
        }

        fun getReceipts(): List<Receipt> {
            return listOf(
                Receipt(1, 779, 0, "2024-09-22", "17:33", true),
                Receipt(1, 2811, 0, "2024-09-21", "18:30", true),
                Receipt(2, 6017, 769, "2024-09-22", "14:17", true),
                Receipt(4, 1224, 1, "2024-09-18", "19:54", true),
                Receipt(5, 824, 59, "2024-08-18", "19:54", true),
                Receipt(5, 9824, 61, "2024-07-18", "19:54", true)
            )
        }

        fun getCategories(): List<Category> {
            return listOf(
                Category("INNE", ChartColors.COLORS[0]),
                Category("JEDZENIE", ChartColors.COLORS[1]),
                Category("ZDROWIE", ChartColors.COLORS[2]),
                Category("KULTURA", ChartColors.COLORS[3]),
                Category("OPŁATY", ChartColors.COLORS[4]),
                Category("KOSMETYKI", ChartColors.COLORS[5]),
                Category("SPORT", ChartColors.COLORS[6]),
                Category("MOTORYZACJA", ChartColors.COLORS[7]),
                Category("UBRANIA", ChartColors.COLORS[8]),
                Category("PALIWO", ChartColors.COLORS[9]),
                Category("ALKOHOL", ChartColors.COLORS[10])
            )
        }

        fun getStores(): List<Store> {
            return listOf(
                Store("9370008168", "Carrefour", 2),
                Store("7791011327", "Biedronka", 2),
                Store("7811897358", "LIDL", 2),
                Store("8992367273", "KAUFLAND", 2),
                Store("8521021463", "NETTO", 2),
                Store("5562125117", "POLOMARKET", 2),
                Store("1070002973", "ALDI", 2),
                Store("5260309174", "AUCHAN", 2),
                Store("5291799498", "ŻABKA", 2),
                Store("7821977018", "INTERMARCHE", 2),
                Store("6211766191", "DINO", 2),
                Store("5252175977", "SUPER-PHARM", 5),
                Store("7270019183", "ROSSMANN", 5),
                Store("2090001776", "HEBE", 5),
                Store("0001112223", "MARTES-SPORT", 6),
                Store("6222797516", "ZAHIR KEBAB", 1),
                Store("7740001454", "ORLEN", 10),
                Store("7790001083", "CIRCLE K", 10),
                Store("9452127910", "BP", 10),
                Store("5261009190", "SHELL", 10),
                Store("5270011878", "MOYA", 10),
                Store("5861988228", "MOL", 10)
            )
        }
    }
}