package com.example.navsample

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource

class ReceiptParserTest {

    var receiptParser = ReceiptParser(0, 0)

    @ParameterizedTest
    @CsvFileSource(resources = ["x.csv"], numLinesToSkip = 1, delimiterString = "|||")
    fun test(line: String, expect: String) {

        val receiptParser = ReceiptParser(0, 0)
        val convertedLines = convertStringToLine(line)
        val result = receiptParser.parseToProducts(convertedLines)[0]
        assertEquals(expect, result.toString())
    }

    private fun convertStringToLine(linesString: String): ArrayList<String> {
        val lines = ArrayList<String>()
        for (line in linesString.split("\n")) {
            if (line.isNotEmpty()) {
                lines.add(line)
            }
        }
        return lines

    }

    @Test
    fun test1() {
        val line = "WIZYTA LEKARSKA 1.00* 150.00 150.00E\nRabat -9,00 141,00E"
        val expect =
            "Product(receiptId=0, name=WIZYTA LEKARSKA, categoryId=1, quantity=1.0, unitPrice=150.0, subtotalPrice=150.0, discount=-9.0, finalPrice=141.0, ptuType=E, raw=WIZYTA LEKARSKA 1.00* 150.00 150.00E)"
        val receiptParser = ReceiptParser(0, 0)
        val convertedLines = convertStringToLine(line)
        val result = receiptParser.parseToProducts(convertedLines)[0]
        assertEquals(expect, result.toString())
    }


    @ParameterizedTest
    @CsvSource(
        "D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= 1,98 0|||0",
        " *__Cheeseburger 1 x4,40 4,40B|||B",

        delimiterString = "|||"
    )
    fun checkPtuType(stringToParse: String, expected: String) {
        //when
        val result = receiptParser.findPtuType(stringToParse)

        //then
        assertEquals(expected, result.data)
    }

    @ParameterizedTest
    @CsvSource(
        "D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= 1,98 0|||1,98|||D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= |1,98| 0",
        " *__Cheeseburger 1 x4,40 4,40B|||4,40|||*__Cheeseburger 1 x4,40 |4,40|B",
        "HP No. 650 (CZ 1szt. x79, 00 79,00A|||79,00|||HP No. 650 (CZ 1szt. x79, 00 |79,00|A",
        "Chust Delikat10X10 A 1 x4, 39 4 39A|||4 39|||Chust Delikat10X10 A 1 x4, 39 |4 39|A",
        "Chust Delikat10X10 A 1 x4  ,  39 4    3 9A|||4    3 9|||Chust Delikat10X10 A 1 x4  ,  39 |4    3 9|A",

        delimiterString = "|||"
    )
    fun checkSubtotalPrice(
        stringToParse: String,
        expectedValue: String,
        markedStringToParse: String
    ) {
        //when
        val result = receiptParser.findSubtotalPrice(stringToParse)

        //then
        assertEquals(expectedValue, result.data)
        val modifiedString = addPipesAtStartAndEndOfFoundString(stringToParse, result)

        assertEquals(markedStringToParse, modifiedString)
    }

    @ParameterizedTest
    @CsvSource(
        "D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= 1,98 0|||37|||3,99|||D JZN ZIEMNIAKI JAD 0,496szt.w*|3,99|= 1,98 0",
        "*__Cheeseburger 1 x4,40 4,40B|||24|||4,40|||*__Cheeseburger 1 x|4,40| 4,40B",
        "HP No. 650 (CZ 1szt. x79, 00 79,00A|||29|||79, 00|||HP No. 650 (CZ 1szt. x|79, 00| 79,00A",
        "Chust Delikat10X10 A 1 x4, 39 4 39A|||30|||4, 39|||Chust Delikat10X10 A 1 x|4, 39| 4 39A",
        "Chust Delikat10X10 A 1 x4  ,  39 4    3 9A|||33|||4  ,  39|||Chust Delikat10X10 A 1 x|4  ,  39| 4    3 9A",

        delimiterString = "|||"
    )
    fun checkUnitPrice(
        stringToParse: String,
        subtotalPriceStartIndex: Int,
        expectedValue: String,
        markedStringToParse: String
    ) {
        //when
        val result = receiptParser.findUnitPrice(stringToParse, subtotalPriceStartIndex)

        //then
        assertEquals(expectedValue, result.data)
        val modifiedString = addPipesAtStartAndEndOfFoundString(stringToParse, result)

        assertEquals(markedStringToParse, modifiedString)
    }

    @ParameterizedTest
    @CsvSource(
        "D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= 1,98 0|||31|||0,496|||D JZN ZIEMNIAKI JAD |0,496|szt.w*3,99= 1,98 0",
        "*__Cheeseburger 1 x4,40 4,40B|||19|||1 |||*__Cheeseburger |1 |x4,40 4,40B",
        "HP No. 650 (CZ 1szt. x79, 00 79,00A|||22|||1|||HP No. 650 (CZ |1|szt. x79, 00 79,00A",
        "Chust Delikat10X10 A 1 x4, 39 4 39A|||24|||1 |||Chust Delikat10X10 A |1 |x4, 39 4 39A",

        delimiterString = "|||"
    )
    fun checkItemQuantity(
        stringToParse: String,
        subtotalPriceStartIndex: Int,
        expectedValue: String,
        markedStringToParse: String
    ) {
        //when
        val result = receiptParser.findQuantity(stringToParse, subtotalPriceStartIndex)

        //then
        assertAll(
            { assertEquals(expectedValue, result.data.trim()) },
            {
                assertEquals(
                    markedStringToParse,
                    addPipesAtStartAndEndOfFoundString(stringToParse, result)
                )
            })

    }

    @ParameterizedTest
    @CsvSource(
        "D JZN ZIEMNIAKI JAD 0,496szt.w*3,99= 1,98 0|||20|||D JZN ZIEMNIAKI JAD",
        "*__Cheeseburger 1 x4,40 4,40B|||16|||*__Cheeseburger",
        "HP No. 650 (CZ 1szt. x79, 00 79,00A|||15|||HP No. 650 (CZ",
        "Chust Delikat10X10 A 1 x4, 39 4 39A|||21|||Chust Delikat10X10 A",

        delimiterString = "|||"
    )
    fun checkName(
        stringToParse: String,
        subtotalPriceStartIndex: Int,
        expectedValue: String
    ) {
        //when
        val result = receiptParser.findName(stringToParse, subtotalPriceStartIndex)

        //then
        assertEquals(expectedValue, result.data.trim())


    }

    private fun addPipesAtStartAndEndOfFoundString(
        stringToParse: String,
        result: ReceiptParser.ReceiptElement
    ): String {
        var modifiedString = stringToParse.substring(
            0,
            result.endIndex
        ) + "|" + stringToParse.substring(result.endIndex, stringToParse.length)
        modifiedString = modifiedString.substring(
            0,
            result.startIndex
        ) + "|" + modifiedString.substring(result.startIndex, modifiedString.length)
        return modifiedString
    }

}