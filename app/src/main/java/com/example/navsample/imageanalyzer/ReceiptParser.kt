package com.example.navsample.imageanalyzer

import com.example.navsample.dto.PriceUtils.Companion.roundInt
import com.example.navsample.entities.database.Product
import kotlin.math.min
import kotlin.math.roundToInt

class ReceiptParser(var receiptId: String, var categoryId: String) {
    companion object {
        private const val REGEX_PRICE = """-*(\d+\s*[,.]\s*\d\s*\d)|(\d+\s+\d\s*\d)"""
        private const val REGEX_QUANTITY = """(\d+[,.]\s*\d*\s*\d*\s*\d*)|(\d+\s*)"""
    }

    data class ReceiptElement(val data: String, val startIndex: Int, val endIndex: Int)

    private fun joinNameAndPrice(names: List<String>, prices: List<String>): ArrayList<String> {
        val sortedProductListOnRecipe = ArrayList<String>()
        val items = min(names.size, prices.size)
        for (i in 0..<items) {
            sortedProductListOnRecipe.add(names[i] + " " + prices[i])
        }
        return sortedProductListOnRecipe
    }

    fun parseToProducts(
        names: List<String>,
        prices: List<String>
    ): ArrayList<Product> {
        val productList = ArrayList<Product>()
        val sortedProductListOnRecipe = joinNameAndPrice(names, prices)
        for (i in 0..sortedProductListOnRecipe.lastIndex) {

            val parsedProduct = parseStringToProduct(sortedProductListOnRecipe[i])
            if (parsedProduct.name == "") {
                parsedProduct.name = names[i]
            }
            if (parsedProduct.discount != 0) {
                val lastProduct = productList.last()
                lastProduct.discount = parsedProduct.discount
                lastProduct.finalPrice = parsedProduct.finalPrice
                continue
            }
            if (parsedProduct.name != "" || parsedProduct.subtotalPrice != 0) {
                parsedProduct.validPrice = arePricesValid(parsedProduct)
                productList.add(parsedProduct)
            }
        }
        return productList
    }

    fun parseStringToProduct(productInformation: String): Product {
        val ptuType = findPtuType(productInformation)
        val subtotalPrice = findSubtotalPrice(productInformation)
        val unitPrice = findUnitPrice(productInformation, subtotalPrice.startIndex)
        val fixedUnitPrice = roundInt(fixPrize(unitPrice.data), 100)
        val fixedSubtotalPrice = roundInt(fixPrize(subtotalPrice.data), 100)
        val name: ReceiptElement
        if (fixedUnitPrice < 0) {
            name = findName(productInformation, unitPrice.startIndex)
            return Product(
                receiptId,
                name.data.trim(),
                categoryId,
                0,
                0,
                0,
                -fixedUnitPrice,
                fixedSubtotalPrice,
                fixPtuType(ptuType.data),
                productInformation,
                false
            )
        } else {
            val quantity = findQuantity(productInformation, unitPrice.startIndex)
            name = findName(productInformation, quantity.startIndex)
            return Product(
                receiptId,
                name.data.trim(),
                categoryId,
                roundInt(fixPrize(quantity.data), 1000),
                fixedUnitPrice,
                fixedSubtotalPrice,
                0,
                fixedSubtotalPrice,
                fixPtuType(ptuType.data),
                productInformation,
                false
            )
        }
    }

    private fun roundInt(integer: Int): Int {
        return (integer / 1000.0).roundToInt()
    }

    private fun arePricesValid(product: Product): Boolean {
        val isNotZero =
            product.quantity > 0.0 && product.unitPrice > 0.0 && product.subtotalPrice > 0.0 && product.finalPrice > 0.0
        val isSubtotalPriceValid =
            roundInt(product.quantity * product.unitPrice) == product.subtotalPrice
        val isFinalPriceValid = product.subtotalPrice - product.discount == product.finalPrice
        return isNotZero && isSubtotalPriceValid && isFinalPriceValid
    }

    private fun fixPrize(price: String): Double {
        var newPrice = price.replace("\\s*".toRegex(), " ").trim()
        newPrice = newPrice.replace(",", ".")
        if (!newPrice.contains(".")) {
            newPrice = newPrice.replaceFirst(" ", ".")
        }
        newPrice = newPrice.replace("\\s*".toRegex(), "").trim()

        return try {
            newPrice.toDouble()
        } catch (exception: Exception) {
            0.0
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

    fun findName(productInfo: String, itemQuantityStartIndex: Int): ReceiptElement {
        if (itemQuantityStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, itemQuantityStartIndex)
            return ReceiptElement(trimProductInfo, 0, itemQuantityStartIndex)
        }

        return ReceiptElement("", -1, -1)
    }

    fun findQuantity(productInfo: String, unitPriceStartIndex: Int): ReceiptElement {

        if (unitPriceStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, unitPriceStartIndex)
            val prices = REGEX_QUANTITY.toRegex().findAll(trimProductInfo).toList()
            if (prices.isEmpty()) {
                return ReceiptElement("", -1, -1)
            }
            val price = prices.last().value
            val startIndex = trimProductInfo.lastIndexOf(price, unitPriceStartIndex - 1, false)
            var endIndex = -1
            if (startIndex != -1) {
                endIndex = startIndex + price.length
            }
            return ReceiptElement(price, startIndex, endIndex)
        }
        return ReceiptElement("", -1, -1)
    }

    fun findUnitPrice(productInfo: String, subtotalPriceStartIndex: Int): ReceiptElement {
        if (subtotalPriceStartIndex > -1) {
            val trimProductInfo = productInfo.substring(0, subtotalPriceStartIndex)

            val prices = REGEX_PRICE.toRegex().findAll(trimProductInfo).toList()
            if (prices.isEmpty()) {
                return ReceiptElement("", -1, -1)
            }
            val price = prices.last().value
            val startIndex = productInfo.lastIndexOf(price, subtotalPriceStartIndex - 1, false)
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

    fun findSubtotalPrice(productInfo: String): ReceiptElement {
        val prices = REGEX_PRICE.toRegex().findAll(productInfo).toList()
        if (prices.isEmpty()) {
            return ReceiptElement("", -1, -1)
        }
        val price = prices.last().value
        val startIndex = productInfo.lastIndexOf(price)


        return ReceiptElement(price, startIndex, startIndex + price.length)
    }
}
