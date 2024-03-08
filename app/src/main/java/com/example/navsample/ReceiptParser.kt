package com.example.navsample

import com.example.navsample.entities.Product

class ReceiptParser(var receiptId: Int) {
    val REGEX_PRICE = """-*(\d+\s*[,.]\s*\d\s*\d)|(\d+\s+\d\s*\d)"""
    val REGEX_AMOUNT = """(\d+[,.]\s*\d*\s*\d*\s*\d*)|(\d+\s*)"""

    data class ReceiptElement(val data: String, val startIndex: Int, val endIndex: Int)


    fun parseToProducts(
        sortedProductListOnRecipe: MutableList<String>,
    ): ArrayList<Product> {
        //SORTOWANIE LISTY PRODUKTOW PO Y
        val productList = ArrayList<Product>()
        for (data in sortedProductListOnRecipe) {
            val parsedProduct = parseStringToProduct(data)
            if (parsedProduct.name != "---" || parsedProduct.finalPrice != -1f) {
                productList.add(parsedProduct)
            }
        }
        return productList
    }


    fun parseStringToProduct(productInformation: String): Product {
        val ptuType = findPtuType(productInformation)
        val finalPrice = findFinalPrice(productInformation)
        val itemPrice = findItemPrice(productInformation, finalPrice.startIndex)
        val itemAmount = findItemAmount(productInformation, itemPrice.startIndex)
        val name = findName(productInformation, itemAmount.startIndex)

        return Product(
            receiptId,
            name.data.trim(),
            1,
            fixPrize(finalPrice.data),
            fixPrize(itemAmount.data),
            fixPrize(itemPrice.data),
            fixPtuType(ptuType.data),
            productInformation
        )
    }


    private fun fixPrize(price: String): Float {
        var newPrice = price.replace("\\s*".toRegex(), " ").trim()
        newPrice = newPrice.replace(",", ".")
        if (!newPrice.contains(".")) {
            newPrice = newPrice.replaceFirst(" ", ".")
        }
        newPrice = newPrice.replace("\\s*".toRegex(), "").trim()

        return try {
            newPrice.toFloat()
        } catch (exception: Exception) {
            0f
        }


    }

    private fun fixPtuType(ptuType: String): String {
        return when (ptuType.uppercase()) {
            "4" -> "A"
            "8" -> "B"
            "0" -> "D"
            "O" -> "D"
            else -> ptuType
        }
    }

    fun findName(productInfo: String, itemAmountStartIndex: Int): ReceiptElement {
        if (itemAmountStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, itemAmountStartIndex)
            return ReceiptElement(trimProductInfo, 0, itemAmountStartIndex)
        }

        return ReceiptElement("", -1, -1)
    }

    fun findItemAmount(productInfo: String, itemPriceStartIndex: Int): ReceiptElement {

        if (itemPriceStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, itemPriceStartIndex)
            val prices = REGEX_AMOUNT.toRegex().findAll(trimProductInfo).toList()
            if (prices.isEmpty()) {
                return ReceiptElement("", -1, -1)
            }
            val price = prices.last().value
            val startIndex = trimProductInfo.lastIndexOf(price, itemPriceStartIndex - 1, false)
            var endIndex = -1
            if (startIndex != -1) {
                endIndex = startIndex + price.length
            }
            return ReceiptElement(price, startIndex, endIndex)
        }
        return ReceiptElement("", -1, -1)
    }

    fun findItemPrice(productInfo: String, finalPriceStartIndex: Int): ReceiptElement {
        if (finalPriceStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, finalPriceStartIndex)

            val prices = REGEX_PRICE.toRegex().findAll(trimProductInfo).toList()
            if (prices.isEmpty()) {
                return ReceiptElement("", -1, -1)
            }
            val price = prices.last().value
            val startIndex = productInfo.lastIndexOf(price, finalPriceStartIndex - 1, false)
            var endIndex = -1
            if (startIndex != -1) {
                endIndex = startIndex + price.length
            }
            return ReceiptElement(price, startIndex, endIndex)
        }
        return ReceiptElement("", -1, -1)
    }

    fun findPtuType(productInformation: String): ReceiptElement {
        val productInfo = productInformation.replace("\\s*".toRegex(), "")
        try {
            for (i in productInfo.length - 1 downTo productInfo.length - 3) {
                if (productInfo[i].isLetterOrDigit()) {
                    return ReceiptElement(productInfo[i].toString(), i, i)
                }
            }
            return ReceiptElement("", -1, -1)
        } catch (ex: StringIndexOutOfBoundsException) {
            return ReceiptElement("", -1, -1)
        }

    }

    fun findFinalPrice(productInfo: String): ReceiptElement {
        val prices = REGEX_PRICE.toRegex().findAll(productInfo).toList()
        if (prices.isEmpty()) {
            return ReceiptElement("", -1, -1)
        }
        val price = prices.last().value
        val startIndex = productInfo.lastIndexOf(price)


        return ReceiptElement(price, startIndex, startIndex + price.length)
    }
}