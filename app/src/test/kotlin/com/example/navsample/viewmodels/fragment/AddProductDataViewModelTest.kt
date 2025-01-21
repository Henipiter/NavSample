package com.example.navsample.viewmodels.fragment

import android.app.Application
import com.example.navsample.R
import com.example.navsample.dto.StringProvider
import com.example.navsample.entities.ReceiptDao
import com.example.navsample.entities.inputs.ProductInputs
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.stream.Stream


class AddProductDataViewModelTest {
    private val receiptDao: ReceiptDao = mock()
    private val application: Application = mock()
    private val stringProvider: StringProvider = mock()
    private val addProductDataViewModel =
        AddProductDataViewModel(application, receiptDao, stringProvider)

    @BeforeEach
    fun setUp() {
        setStringProviderMock()
    }


    private fun setStringProviderMock() {
        `when`(stringProvider.getString(R.string.suggestion_prefix)).thenReturn("Maybe")
        `when`(stringProvider.getString(R.string.second_suggestion_prefix)).thenReturn("or")
        `when`(stringProvider.getString(R.string.empty_value_error)).thenReturn("Empty")
        `when`(stringProvider.getString(R.string.bad_value_error)).thenReturn("Error")

    }

    @ParameterizedTest
    @MethodSource("quantityValuesProviderWithNull")
    fun validateQuantityWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateQuantity(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("quantityValuesProviderWithEmpty")
    fun validateQuantityWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateQuantity(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("unitPriceValuesProviderWithNull")
    fun validateUnitPriceWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateUnitPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("unitPriceValuesProviderWithEmpty")
    fun validateUnitPriceWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateUnitPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("finalPriceValuesProviderWithNull")
    fun validateFinalPriceWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateFinalPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("finalPriceValuesProviderWithEmpty")
    fun validateFinalPriceWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateFinalPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("discountPriceValuesProviderWithNull")
    fun validateDiscountWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateDiscount(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("discountPriceValuesProviderWithEmpty")
    fun validateDiscountWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateDiscount(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithUnitAndQuantityValuesProviderWithNull")
    fun validateSubtotalWithUnitAndQuantityValuesWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithUnitAndQuantityValuesProviderWithEmpty")
    fun validateSubtotalWithUnitAndQuantityValuesWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithFinalAndDiscountValuesProviderWithNull")
    fun validateSubtotalWithFinalAndDiscountValuesWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithFinalAndDiscountValuesProviderWithEmpty")
    fun validateSubtotalWithFinalAndDiscountValuesWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithAllValuesWithTwoSuggestionsProviderWithNull")
    fun validateSubtotalWithAllValuesWithNull(
        productInputs: ProductInputs,
        expectedSuggestion1: String?,
        expectedSuggestion2: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion1, suggestionMessage.firstSuggestion) },
            { assertEquals(expectedSuggestion2, suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithAllValuesWithTwoSuggestionsProviderWithEmpty")
    fun validateSubtotalWithAllValuesWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion1: String?,
        expectedSuggestion2: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion1, suggestionMessage.firstSuggestion) },
            { assertEquals(expectedSuggestion2, suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithAllValuesProviderWithNull")
    fun validateSubtotalWithAllValuesWithNull(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithAllValuesProviderWithEmpty")
    fun validateSubtotalWithAllValuesWithEmpty(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateSubtotalPrice(productInputs)

        //then
        assertAll(
            { assertEquals(expectedSuggestion, suggestionMessage.firstSuggestion) },
            { assertNull(suggestionMessage.secondSuggestion) }
        )
    }


    companion object {
        @JvmStatic
        fun quantityValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice(null, "2", "2.11"), "Maybe 1.055"),
                Arguments.of(getForPrice("1", "2", "2.11"), "Maybe 1.055"),
                Arguments.of(getForPrice("1", null, "2.11"), null),
                Arguments.of(getForPrice("1", "2", null), null),
                Arguments.of(getForPrice("1", null, null), null),
                Arguments.of(getForPrice(null, null, "2.11"), "Maybe 1.000"),
                Arguments.of(getForPrice(null, "2", null), "Maybe 1.000"),
                Arguments.of(getForPrice(null, null, null), "Maybe 1.000")
            )
        }

        @JvmStatic
        fun quantityValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("", "2", "2.11"), "Maybe 1.055"),
                Arguments.of(getForPrice("1", "", "2.11"), null),
                Arguments.of(getForPrice("1", "2", ""), null),
                Arguments.of(getForPrice("1", "", ""), null),
                Arguments.of(getForPrice("", "", "2.11"), "Maybe 1.000"),
                Arguments.of(getForPrice("", "2", ""), "Maybe 1.000"),
                Arguments.of(getForPrice("", "", ""), "Maybe 1.000")
            )
        }


        @JvmStatic
        fun unitPriceValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", null, "2.11"), "Maybe 2.00"),
                Arguments.of(getForPrice("1.055", "3", "2.11"), "Maybe 2.00"),
                Arguments.of(getForPrice(null, "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", "2", null), null),
                Arguments.of(getForPrice(null, "2", null), null),
                Arguments.of(getForPrice(null, null, "2.11"), "Maybe 1.00"),
                Arguments.of(getForPrice("1.055", null, null), "Maybe 1.00"),
                Arguments.of(getForPrice(null, null, null), "Maybe 1.00")
            )
        }

        @JvmStatic
        fun unitPriceValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "", "2.11"), "Maybe 2.00"),
                Arguments.of(getForPrice("", "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", "2", ""), null),
                Arguments.of(getForPrice("", "2", ""), null),
                Arguments.of(getForPrice("", "", "2.11"), "Maybe 1.00"),
                Arguments.of(getForPrice("1.055", "", ""), "Maybe 1.00"),
                Arguments.of(getForPrice("", "", ""), "Maybe 1.00")
            )
        }

        @JvmStatic
        fun finalPriceValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), "Maybe 0.90"),
                Arguments.of(getForFinal("1.05", "0.15", "2.1"), "Maybe 0.90"),
                Arguments.of(getForFinal(null, "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", null, "0.9"), null),
                Arguments.of(getForFinal(null, null, "0.9"), null),
                Arguments.of(getForFinal(null, "0.15", null), "Empty"),
                Arguments.of(getForFinal("1.05", null, null), "Empty"),
                Arguments.of(getForFinal(null, null, null), "Empty"),
            )
        }

        @JvmStatic
        fun finalPriceValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", ""), "Maybe 0.90"),
                Arguments.of(getForFinal("", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "", "0.9"), null),
                Arguments.of(getForFinal("", "", "0.9"), null),
                Arguments.of(getForFinal("", "0.15", ""), "Empty"),
                Arguments.of(getForFinal("1.05", "", ""), "Empty"),
                Arguments.of(getForFinal("", "", ""), "Empty"),
            )
        }

        @JvmStatic
        fun discountPriceValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", null, "0.9"), "Maybe 0.15"),
                Arguments.of(getForFinal("1.05", "0.5", "0.9"), "Maybe 0.15"),
                Arguments.of(getForFinal(null, "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), null),
                Arguments.of(getForFinal(null, "0.15", null), null),
                Arguments.of(getForFinal(null, null, "0.9"), "Maybe 0.00"),
                Arguments.of(getForFinal("1.05", null, null), "Maybe 0.00"),
                Arguments.of(getForFinal(null, null, null), "Maybe 0.00"),
            )
        }

        @JvmStatic
        fun discountPriceValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", null, "0.9"), "Maybe 0.15"),
                Arguments.of(getForFinal("1.05", "0.5", "0.9"), "Maybe 0.15"),
                Arguments.of(getForFinal(null, "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), null),
                Arguments.of(getForFinal(null, "0.15", null), null),
                Arguments.of(getForFinal(null, null, "0.9"), "Maybe 0.00"),
                Arguments.of(getForFinal("1.05", null, null), "Maybe 0.00"),
                Arguments.of(getForFinal(null, null, null), "Maybe 0.00"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithUnitAndQuantityValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", ""), "Maybe 2.11"),
                Arguments.of(getForPrice("", "2", "2"), null),
                Arguments.of(getForPrice("1.055", "", "2"), null),
                Arguments.of(getForPrice("", "", "2"), null),
                Arguments.of(getForPrice("", "2", ""), "Empty"),
                Arguments.of(getForPrice("1.055", "", ""), "Empty"),
                Arguments.of(getForPrice("", "", ""), "Empty"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithUnitAndQuantityValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", "2", null), "Maybe 2.11"),
                Arguments.of(getForPrice("1.055", "2", "2"), "Maybe 2.11"),
                Arguments.of(getForPrice(null, "2", "2"), null),
                Arguments.of(getForPrice("1.055", null, "2"), null),
                Arguments.of(getForPrice(null, null, "2"), null),
                Arguments.of(getForPrice(null, "2", null), "Empty"),
                Arguments.of(getForPrice("1.055", null, null), "Empty"),
                Arguments.of(getForPrice(null, null, null), "Empty"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithFinalAndDiscountValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("", "0.15", "0.9"), "Maybe 1.05"),
                Arguments.of(getForFinal("1.05", "", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", ""), null),
                Arguments.of(getForFinal("1.05", "", ""), null),
                Arguments.of(getForFinal("", "", "0.9"), "Empty"),
                Arguments.of(getForFinal("", "0.15", ""), "Empty"),
                Arguments.of(getForFinal("", "", ""), "Empty"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithFinalAndDiscountValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("", "0.15", "0.9"), "Maybe 1.05"),
                Arguments.of(getForFinal("1", "0.15", "0.9"), "Maybe 1.05"),
                Arguments.of(getForFinal("1.05", "", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", ""), null),
                Arguments.of(getForFinal("1.05", "", ""), null),
                Arguments.of(getForFinal("", "", "0.9"), "Empty"),
                Arguments.of(getForFinal("", "0.15", ""), "Empty"),
                Arguments.of(getForFinal("", "", ""), "Empty"),
            )
        }


        @JvmStatic
        fun subtotalPriceWithAllValuesProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForSubtotal("1.055", "2", "2.11", "0.21", "1.9"), null),

                Arguments.of(getForSubtotal("1.055", "2", null, "0.21", "1.9"), "Maybe 2.11"),
                Arguments.of(getForSubtotal("1.055", null, "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal(null, "2", "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", null, "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", "0.21", null), null),

                Arguments.of(getForSubtotal(null, "2", "2.11", "0.21", null), null),
                Arguments.of(getForSubtotal("1.055", null, "2.11", null, "1.9"), null),
                Arguments.of(getForSubtotal("1.055", null, "2.11", "0.21", null), null),
                Arguments.of(getForSubtotal(null, "2", "2.11", null, "1.9"), null),

                Arguments.of(getForSubtotal(null, "2", null, "0.21", null), "Empty"),
                Arguments.of(getForSubtotal("1.055", null, null, null, "1.9"), "Empty"),
                Arguments.of(getForSubtotal("1.055", null, null, "0.21", null), "Empty"),
                Arguments.of(getForSubtotal(null, "2", null, null, "1.9"), "Empty"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithAllValuesProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForSubtotal("1.055", "2", "", "0.21", "1.9"), "Maybe 2.11"),
                Arguments.of(getForSubtotal("1.055", "", "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal("", "2", "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", "", "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", "0.21", ""), null),

                Arguments.of(getForSubtotal("", "2", "2.11", "0.21", ""), null),
                Arguments.of(getForSubtotal("1.055", "", "2.11", "", "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "", "2.11", "0.21", ""), null),
                Arguments.of(getForSubtotal("", "2", "2.11", "", "1.9"), null),

                Arguments.of(getForSubtotal("", "2", "", "0.21", ""), "Empty"),
                Arguments.of(getForSubtotal("1.055", "", "", "", "1.9"), "Empty"),
                Arguments.of(getForSubtotal("1.055", "", "", "0.21", ""), "Empty"),
                Arguments.of(getForSubtotal("", "2", "", "", "1.9"), "Empty"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithAllValuesWithTwoSuggestionsProviderWithNull(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    getForSubtotal("1.055", "2", null, "0.5", "1.1"),
                    "Maybe 2.11", " or 1.60"
                ),
                Arguments.of(
                    getForSubtotal("1.055", "2", "0.5", "0.5", "1.1"),
                    "Maybe 2.11", " or 1.60"
                ),

                )
        }

        @JvmStatic
        fun subtotalPriceWithAllValuesWithTwoSuggestionsProviderWithEmpty(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    getForSubtotal("1.055", "2", "", "0.5", "1.1"),
                    "Maybe 2.11", " or 1.60"
                )

            )
        }


        private fun getForPrice(
            quantity: String?, unitPrice: String?, subtotalPrice: String?
        ): ProductInputs {
            return ProductInputs(
                quantity = quantity, unitPrice = unitPrice, subtotalPrice = subtotalPrice
            )
        }

        private fun getForFinal(
            subtotalPrice: String?, discount: String?, finalPrice: String?
        ): ProductInputs {
            return ProductInputs(
                subtotalPrice = subtotalPrice, discount = discount, finalPrice = finalPrice
            )
        }

        private fun getForSubtotal(
            quantity: String?,
            unitPrice: String?,
            subtotalPrice: String?,
            discount: String?,
            finalPrice: String?
        ): ProductInputs {
            return ProductInputs(
                quantity = quantity,
                unitPrice = unitPrice,
                subtotalPrice = subtotalPrice,
                discount = discount,
                finalPrice = finalPrice
            )
        }
    }
}
