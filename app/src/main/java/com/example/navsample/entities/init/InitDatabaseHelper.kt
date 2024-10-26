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
                        "f37ac640-b7fd-451d-aa94-d52451dc98ba",
                        "D_JZN_ZIEMNIAKI JAD",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        496, 399, 198, 0, 198, "D", "", true
                    ),
                    Product(
                        "f37ac640-b7fd-451d-aa94-d52451dc98ba",
                        "D_WAFLE_SONKO 130G",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 359, 359, 0, 359, "D", "", true
                    ),
                    Product(
                        "f37ac640-b7fd-451d-aa94-d52451dc98ba",
                        "D_KAJZERKA 50G",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        6000, 37, 222, 0, 222, "D", "", true
                    )
                ),
                listOf(
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_KAJZERKA 50G",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        8000, 37, 296, 0, 296, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_SEREK_ALMETTE 150",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 499, 499, 0, 499, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_MC SALADA LODOWA",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 499, 499, 0, 499, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_RZODKIEWKA PECZEK",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 229, 229, 0, 229, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_SER MAGNAT",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        204, 3990, 814, 0, 814, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_GREJPFRUTY BIALE L",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        348, 769, 268, 0, 268, "D", "", true
                    ),
                    Product(
                        "672aae7c-0ae1-445e-ad23-c31666368a02",
                        "D_MC CYTRYNY LUZ",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        258, 799, 206, 0, 206, "D", "", true
                    )
                ),
                listOf(
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "ChlebWiel400g 50G",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 359, 359, 0, 359, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "Papier Queen 10r 150",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        1000, 499, 499, 0, 499, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "Chust Delikat x150",
                        "e70f3a23-97c3-409d-bea7-89e9b648f2ca",
                        100, 439, 439, 0, 439, "A", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "Chust Delikat x150",
                        "e70f3a23-97c3-409d-bea7-89e9b648f2ca",
                        100, 439, 439, 0, 439, "A", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "Chust Delikat 10x10",
                        "e70f3a23-97c3-409d-bea7-89e9b648f2ca",
                        100, 439, 439, 0, 439, "A", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "MakaronRyzAsia200g",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        100, 599, 599, 0, 599, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "SmootZOwocLes075",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        100, 666, 666, 0, 666, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "Marchew luz",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        335, 299, 100, 0, 100, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "JablPolskieGalaLuz",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        395, 349, 138, 0, 138, "D", "", true
                    ),
                    Product(
                        "f8f0fc36-3d41-428d-8eae-6503c139d974",
                        "CebulaZolta Luz",
                        "bcf97011-db56-44c3-bf6a-388c7e70da7b",
                        8, 499, 40, 0, 40, "D", "", true
                    )
                ),
                listOf(
                    Product(
                        "c3401927-875d-4d45-94bb-60b1331502c7",
                        "IBUPROM 200MG 10 TABL",
                        "9412d154-fcd1-4491-9f9d-6f2ed9507248",
                        1000, 824, 359, 0, 359, "D", "", true
                    )
                ),
                listOf(
                    Product(
                        "cf181987-aa29-4769-a42a-0f72db0bd7ef",
                        "KAPIELOWKI",
                        "377057f5-b5b5-4318-83e0-de4c91aa3af2",
                        1000, 8999, 8999, 0, 8999, "D", "", true
                    ),
                    Product(
                        "cf181987-aa29-4769-a42a-0f72db0bd7ef",
                        "BUTY BIEGANIE",
                        "377057f5-b5b5-4318-83e0-de4c91aa3af2",
                        1000, 29999, 29999, 0, 29999, "D", "", true
                    )
                )
            )
        }

        fun getReceipts(): List<Receipt> {
            val list = listOf(
                Receipt(
                    "5da5e08b-9c26-43c7-9d7b-a623f24c7b37",
                    779, 0, "2024-09-22", "17:33", true
                ),
                Receipt(
                    "5da5e08b-9c26-43c7-9d7b-a623f24c7b37",
                    2811, 0, "2024-09-21", "18:30", true
                ),
                Receipt(
                    "5fa5bc53-50e3-40e2-9c90-8a225dc7d174",
                    6017, 769, "2024-09-22", "14:17", true
                ),
                Receipt(
                    "edd4ffcd-7f10-45fc-aad8-f5888e4139ea",
                    1224, 1, "2024-09-18", "19:54", true
                ),
                Receipt(
                    "8ec94a66-72b1-4895-ac76-f00bae6b62a5",
                    824, 59, "2024-08-18", "19:54", true
                ),
                Receipt(
                    "8ec94a66-72b1-4895-ac76-f00bae6b62a5",
                    9824, 61, "2024-07-18", "19:54", true
                )
            )
            list[0].id = "f37ac640-b7fd-451d-aa94-d52451dc98ba"
            list[1].id = "672aae7c-0ae1-445e-ad23-c31666368a02"
            list[2].id = "f8f0fc36-3d41-428d-8eae-6503c139d974"
            list[3].id = "c3401927-875d-4d45-94bb-60b1331502c7"
            list[4].id = "cf181987-aa29-4769-a42a-0f72db0bd7ef"
            list[5].id = "111d63fd-00a0-4b2c-bc4a-539c2c65eb78"
            return list
        }

        fun getCategories(): List<Category> {
            val list = listOf(
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
            list[0].id = "bcf97011-db56-44c3-bf6a-388c7e70da7b"
            list[1].id = "9412d154-fcd1-4491-9f9d-6f2ed9507248"
            list[2].id = "4f3e7126-c82a-4d9a-a900-485755087a4d"
            list[3].id = "548152da-74da-4828-bbe2-f26c05df8122"
            list[4].id = "e70f3a23-97c3-409d-bea7-89e9b648f2ca"
            list[5].id = "341e32dc-08f7-4342-a527-c6936c1b645a"
            list[6].id = "377057f5-b5b5-4318-83e0-de4c91aa3af2"
            list[7].id = "82502485-f3d5-413c-931a-c7e0fe9b6829"
            list[8].id = "7ab05e9c-ab28-4c92-8bfe-cdf994f27436"
            list[9].id = "78fcfdc6-00bb-4739-a473-482b84f0c637"
            list[10].id = "1206ccfb-40d6-4ea3-b7b8-e2f77924096c"
            return list
        }

        fun getStores(): List<Store> {
            val list = listOf(
                Store("9370008168", "Carrefour", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("7791011327", "Biedronka", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("7811897358", "LIDL", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("8992367273", "KAUFLAND", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("8521021463", "NETTO", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("5562125117", "POLOMARKET", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("1070002973", "ALDI", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("5260309174", "AUCHAN", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("5291799498", "ŻABKA", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("7821977018", "INTERMARCHE", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("6211766191", "DINO", "9412d154-fcd1-4491-9f9d-6f2ed9507248"),
                Store("5252175977", "SUPER-PHARM", "e70f3a23-97c3-409d-bea7-89e9b648f2ca"),
                Store("7270019183", "ROSSMANN", "e70f3a23-97c3-409d-bea7-89e9b648f2ca"),
                Store("2090001776", "HEBE", "e70f3a23-97c3-409d-bea7-89e9b648f2ca"),
                Store("0001112223", "MARTES-SPORT", "341e32dc-08f7-4342-a527-c6936c1b645a"),
                Store("6222797516", "ZAHIR KEBAB", "bcf97011-db56-44c3-bf6a-388c7e70da7b"),
                Store("7740001454", "ORLEN", "78fcfdc6-00bb-4739-a473-482b84f0c637"),
                Store("7790001083", "CIRCLE K", "78fcfdc6-00bb-4739-a473-482b84f0c637"),
                Store("9452127910", "BP", "78fcfdc6-00bb-4739-a473-482b84f0c637"),
                Store("5261009190", "SHELL", "78fcfdc6-00bb-4739-a473-482b84f0c637"),
                Store("5270011878", "MOYA", "78fcfdc6-00bb-4739-a473-482b84f0c637"),
                Store("5861988228", "MOL", "78fcfdc6-00bb-4739-a473-482b84f0c637")
            )
            list[0].id = "5da5e08b-9c26-43c7-9d7b-a623f24c7b37"
            list[1].id = "5fa5bc53-50e3-40e2-9c90-8a225dc7d174"
            list[2].id = "ce283505-f230-45ac-908c-55071c8fb3fd"
            list[3].id = "edd4ffcd-7f10-45fc-aad8-f5888e4139ea"
            list[4].id = "8ec94a66-72b1-4895-ac76-f00bae6b62a5"
            list[5].id = "4c414a0c-5018-49fe-a20d-70bc977d461e"
            list[6].id = "64d98743-2764-4d42-9c97-efd05510eaeb"
            list[7].id = "71a5361e-0ff7-46b5-ac92-1af298a7b1e3"
            list[8].id = "e5ada9cf-b4f0-450d-ab53-ade691dae1cc"
            list[9].id = "1248ed91-7221-41d0-b5d8-dae1f02ccaf5"
            list[10].id = "b3d6b977-8142-4e88-be33-af878432d3a1"
            list[11].id = "c97c69b2-b289-4a88-9b04-8bca179622b0"
            list[12].id = "a95a8879-c2f5-4982-b482-37006c295470"
            list[13].id = "sd6d9d28-1ed8-4947-91e0-09248b3f91f5"
            list[14].id = "853bf561-09a5-4e4f-be8e-ef6377940f44"
            list[15].id = "aabe588e-ca8b-4e70-a827-01f0f55bad44"
            list[16].id = "b1876018-e56c-4828-8ae2-2ac9ab59ef8e"
            list[17].id = "9209f0ea-d37e-45da-9d99-79e4a70f5662"
            list[18].id = "80541d72-1e4d-44f8-8388-a1993ed642b9"
            list[19].id = "d122b4de-0c76-4db9-a4a6-1edc0389c3b0"
            list[20].id = "898462cc-ff1e-4478-b323-4ca6f942a13b"
            list[21].id = "6ebf7f1d-6208-4e91-ac30-ec193b573da1"
            return list
        }


    }
}