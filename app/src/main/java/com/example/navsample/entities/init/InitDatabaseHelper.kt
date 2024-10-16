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
                        0.496,
                        3.99,
                        1.98,
                        0.0,
                        1.98,
                        "D",
                        "",
                        true
                    ),
                    Product(1, "D_WAFLE_SONKO 130G", 1, 1.0, 3.59, 3.59, 0.0, 3.59, "D", "", true),
                    Product(1, "D_KAJZERKA 50G", 1, 6.0, 0.37, 2.22, 0.0, 2.22, "D", "", true)
                ),
                listOf(
                    Product(2, "D_KAJZERKA 50G", 1, 8.0, 0.37, 2.96, 0.0, 2.96, "D", "", true),
                    Product(2, "D_SEREK_ALMETTE 150", 1, 1.0, 4.99, 4.99, 0.0, 4.99, "D", "", true),
                    Product(2, "D_MC SALADA LODOWA", 1, 1.0, 4.99, 4.99, 0.0, 4.99, "D", "", true),
                    Product(2, "D_RZODKIEWKA PECZEK", 1, 1.0, 2.29, 2.29, 0.0, 2.29, "D", "", true),
                    Product(2, "D_SER MAGNAT", 1, 0.204, 39.90, 8.14, 0.0, 8.14, "D", "", true),
                    Product(
                        2,
                        "D_GREJPFRUTY BIALE L",
                        1,
                        0.348,
                        7.69,
                        2.68,
                        0.0,
                        2.68,
                        "D",
                        "",
                        true
                    ),
                    Product(2, "D_MC CYTRYNY LUZ", 1, 0.258, 7.99, 2.06, 0.0, 2.06, "D", "", true)
                ),
                listOf(
                    Product(3, "ChlebWiel400g 50G", 1, 1.0, 3.59, 3.59, 0.0, 3.59, "D", "", true),
                    Product(
                        3,
                        "Papier Queen 10r 150",
                        1,
                        1.0,
                        4.99,
                        4.99,
                        0.0,
                        4.99,
                        "D",
                        "",
                        true
                    ),
                    Product(3, "Chust Delikat x150", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", "", true),
                    Product(3, "Chust Delikat x150", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", "", true),
                    Product(3, "Chust Delikat 10x10", 5, 1.0, 4.39, 4.39, 0.0, 4.39, "A", "", true),
                    Product(3, "MakaronRyzAsia200g", 1, 1.0, 5.99, 5.99, 0.0, 5.99, "D", "", true),
                    Product(3, "SmootZOwocLes0.75", 1, 1.0, 6.66, 6.66, 0.0, 6.66, "D", "", true),
                    Product(3, "Marchew luz", 1, 0.335, 2.99, 1.0, 0.0, 1.0, "D", "", true),
                    Product(
                        3,
                        "JablPolskieGalaLuz",
                        1,
                        0.395,
                        3.49,
                        1.38,
                        0.0,
                        1.38,
                        "D",
                        "",
                        true
                    ),
                    Product(3, "CebulaZolta Luz", 1, 0.08, 4.99, 0.4, 0.0, 0.4, "D", "", true)
                ),
                listOf(
                    Product(
                        4,
                        "IBUPROM 200MG 10 TABL",
                        2,
                        1.0,
                        8.24,
                        3.59,
                        0.0,
                        3.59,
                        "D",
                        "",
                        true
                    )
                ),
                listOf(
                    Product(5, "KAPIELOWKI", 7, 1.0, 89.99, 89.99, 0.0, 89.99, "D", "", true),
                    Product(5, "BUTY BIEGANIE", 7, 1.0, 299.99, 299.99, 0.0, 299.99, "D", "", true)
                ),
                listOf(
                    Product(6, "KAPIELOWKI", 7, 1.0, 89.99, 89.99, 0.0, 89.99, "D", "", true),
                    Product(6, "BUTY BIEGANIE", 7, 1.0, 299.99, 299.99, 0.0, 299.99, "D", "", true)
                )
            )
        }

        fun getReceipts(): List<Receipt> {
            return listOf(
                Receipt(1, 7.79, 0.0, "2024-09-22", "17:33", true),
                Receipt(1, 28.11, 0.0, "2024-09-21", "18:30", true),
                Receipt(2, 60.17, 7.69, "2024-09-22", "14:17", true),
                Receipt(4, 12.24, 0.01, "2024-09-18", "19:54", true),
                Receipt(5, 8.24, 0.59, "2024-08-18", "19:54", true),
                Receipt(5, 98.24, 0.61, "2024-07-18", "19:54", true)
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