package com.example.navsample

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import java.text.Normalizer
import java.util.regex.Pattern

class ReceiptParserTest {

    var receiptParser = ReceiptParser()

    @ParameterizedTest
    @CsvFileSource(resources = ["x.csv"], numLinesToSkip = 1, delimiterString = "|||")
    fun test(line:String, expect:String) {

        val receiptParser =  ReceiptParser()
        val convertedLines = convertStringToLine(line)
        val result = receiptParser.parseToProducts(convertedLines)[0]
        assertEquals(expect, result.toString())
    }

    private fun convertStringToLine(linesString: String): ArrayList<ImageAnalyzer.Line> {
        val lines = ArrayList<ImageAnalyzer.Line>()
        for (line in linesString.split("\n")) {
            if (line.isNotEmpty()) {
                lines.add(ImageAnalyzer.Line(line))
            }
        }
        return lines

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
    fun checkFinalPrice(stringToParse: String, expectedValue: String, markedStringToParse: String) {
        //when
        val result = receiptParser.findFinalPrice(stringToParse)

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
    fun checkItemPrice(
        stringToParse: String,
        finalPriceStartIndex: Int,
        expectedValue: String,
        markedStringToParse: String
    ) {
        //when
        val result = receiptParser.findItemPrice(stringToParse, finalPriceStartIndex)

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
    fun checkItemAmount(
        stringToParse: String,
        finalPriceStartIndex: Int,
        expectedValue: String,
        markedStringToParse: String
    ) {
        //when
        val result = receiptParser.findItemAmount(stringToParse, finalPriceStartIndex)

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
        finalPriceStartIndex: Int,
        expectedValue: String
    ) {
        //when
        val result = receiptParser.findName(stringToParse, finalPriceStartIndex)

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