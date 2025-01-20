package com.example.navsample.viewmodels.fragment

import android.app.Application
import com.example.navsample.R
import com.example.navsample.dto.StringProvider
import com.example.navsample.entities.ReceiptDao
import com.example.navsample.entities.inputs.ProductFinalPriceInputs
import com.example.navsample.entities.inputs.ProductInputs
import com.example.navsample.entities.inputs.ProductPriceInputs
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    @Test
    fun validateObligatoryFields() {
    }

    @Test
    fun validatePricesIfAllNull() {
        //given
        val productInputs = ProductPriceInputs(null, null, null)

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.quantitySuggestion) },
            { assertEquals(null, errors.unitPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(" ", errors.quantityError) },
            { assertEquals(" ", errors.unitPriceError) },
            { assertEquals(" ", errors.subtotalPriceError) },
        )
    }

    @Test
    fun validatePricesIfAllFilledButWrong() {
        //given
        val productInputs = ProductPriceInputs("1.055", "2", "1")

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals("Maybe 0.500", errors.quantitySuggestion) },
            { assertEquals("Maybe 0.94", errors.unitPriceSuggestion) },
            { assertEquals("Maybe 2.11", errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.quantityError) },
            { assertEquals(null, errors.unitPriceError) },
            { assertEquals(null, errors.subtotalPriceError) },
        )
    }

    @Test
    fun validatePricesIfAllFilledCorrect() {
        //given
        val productInputs = ProductPriceInputs("1.055", "2", "2.11")

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.quantitySuggestion) },
            { assertEquals(null, errors.unitPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.quantityError) },
            { assertEquals(null, errors.unitPriceError) },
            { assertEquals(null, errors.subtotalPriceError) },
        )
    }

    @Test
    fun validatePricesIfNotFilledQuantity() {
        //given
        val productInputs = ProductPriceInputs(null, "2", "2.11")

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals("Maybe 1.055", errors.quantitySuggestion) },
            { assertEquals(null, errors.unitPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(" ", errors.quantityError) },
            { assertEquals(null, errors.unitPriceError) },
            { assertEquals(null, errors.subtotalPriceError) },
        )
    }

    @Test
    fun validatePricesIfNotFilledUnitPrice() {
        //given
        val productInputs = ProductPriceInputs("1.055", null, "2.11")

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.quantitySuggestion) },
            { assertEquals("Maybe 2.00", errors.unitPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.quantityError) },
            { assertEquals(" ", errors.unitPriceError) },
            { assertEquals(null, errors.subtotalPriceError) },
        )
    }

    @Test
    fun validatePricesIfNotFilledSubtotalPrice() {
        //given
        val productInputs = ProductPriceInputs("1.055", "2", null)

        //when
        val errors = addProductDataViewModel.validatePrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.quantitySuggestion) },
            { assertEquals(null, errors.unitPriceSuggestion) },
            { assertEquals("Maybe 2.11", errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.quantityError) },
            { assertEquals(null, errors.unitPriceError) },
            { assertEquals(" ", errors.subtotalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfAllNull() {
        //given
        val productInputs = ProductFinalPriceInputs(null, null, null)

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals("Maybe 0.00", errors.discountSuggestion) },
            { assertEquals(null, errors.finalPriceSuggestion) },
            { assertEquals(" ", errors.subtotalPriceError) },
            { assertEquals(" ", errors.discountError) },
            { assertEquals(" ", errors.finalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfAllFilledButWrong() {
        //given
        val productInputs = ProductFinalPriceInputs("1.05", "0.1", "0.9")

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals("Maybe 0.15", errors.discountSuggestion) },
            { assertEquals("Maybe 0.95", errors.finalPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceError) },
            { assertEquals(" ", errors.discountError) },
            { assertEquals(" ", errors.finalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfAllFilledCorrect() {
        //given
        val productInputs = ProductFinalPriceInputs("1.05", "0.15", "0.9")

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.discountSuggestion) },
            { assertEquals(null, errors.finalPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceError) },
            { assertEquals(null, errors.discountError) },
            { assertEquals(null, errors.finalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfNotFilledSubtotalPrice() {
        //given
        val productInputs = ProductFinalPriceInputs(null, "0.15", "0.9")

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.discountSuggestion) },
            { assertEquals(null, errors.finalPriceSuggestion) },
            { assertEquals(" ", errors.subtotalPriceError) },
            { assertEquals(null, errors.discountError) },
            { assertEquals(null, errors.finalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfNotFilledDiscount() {
        //given
        val productInputs = ProductFinalPriceInputs("1.05", null, "0.9")

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals("Maybe 0.15", errors.discountSuggestion) },
            { assertEquals(null, errors.finalPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceError) },
            { assertEquals(" ", errors.discountError) },
            { assertEquals(null, errors.finalPriceError) },
        )
    }

    @Test
    fun validateFinalPricesIfNotFilledFinalPrice() {
        //given
        val productInputs = ProductFinalPriceInputs("1.05", "0.15", null)

        //when
        val errors = addProductDataViewModel.validateFinalPrices(productInputs)

        //then
        assertAll(
            { assertEquals(null, errors.subtotalPriceSuggestion) },
            { assertEquals(null, errors.discountSuggestion) },
            { assertEquals("Maybe 0.90", errors.finalPriceSuggestion) },
            { assertEquals(null, errors.subtotalPriceError) },
            { assertEquals(null, errors.discountError) },
            { assertEquals(" ", errors.finalPriceError) },
        )
    }

    private fun setStringProviderMock() {
        `when`(stringProvider.getString(R.string.suggestion_prefix)).thenReturn("Maybe")
        `when`(stringProvider.getString(R.string.empty_value_error)).thenReturn("Empty")
        `when`(stringProvider.getString(R.string.bad_value_error)).thenReturn("Error")

    }

    @ParameterizedTest
    @MethodSource("quantityValuesProvider")
    fun validateQuantity(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateQuantity(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("unitPriceValuesProvider")
    fun validateUnitPrice(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateUnitPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("finalPriceValuesProvider")
    fun validateFinalPrice(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateFinalPrice(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("discountPriceValuesProvider")
    fun validateDiscount(
        productInputs: ProductInputs,
        expectedSuggestion: String?
    ) {
        //when
        val suggestionMessage = addProductDataViewModel.validateDiscount(productInputs)

        //then
        assertEquals(expectedSuggestion, suggestionMessage)
    }

    @ParameterizedTest
    @MethodSource("subtotalPriceWithUnitAndQuantityValuesProvider")
    fun validateSubtotalWithUnitAndQuantityValues(
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
    @MethodSource("subtotalPriceWithFinalAndDiscountValuesProvider")
    fun validateSubtotalWithFinalAndDiscountValues(
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
    @MethodSource("subtotalPriceWithAllValuesWithTwoSuggestionsProvider")
    fun validateSubtotalWithAllValues(
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
    @MethodSource("subtotalPriceWithAllValuesProvider")
    fun validateSubtotalWithAllValues(
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
        fun quantityValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice(null, "2", "2.11"), "1.055"),
                Arguments.of(getForPrice("1", "2", "2.11"), "1.055"),
                Arguments.of(getForPrice("1", null, "2.11"), null),
                Arguments.of(getForPrice("1", "2", null), null),
                Arguments.of(getForPrice("1", null, null), null),
                Arguments.of(getForPrice(null, null, "2.11"), "1.000"),
                Arguments.of(getForPrice(null, "2", null), "1.000"),
                Arguments.of(getForPrice(null, null, null), "1.000")
            )
        }


        @JvmStatic
        fun unitPriceValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", null, "2.11"), "2.00"),
                Arguments.of(getForPrice("1.055", "3", "2.11"), "2.00"),
                Arguments.of(getForPrice(null, "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", "2", null), null),
                Arguments.of(getForPrice(null, "2", null), null),
                Arguments.of(getForPrice(null, null, "2.11"), "1.00"),
                Arguments.of(getForPrice("1.055", null, null), "1.00"),
                Arguments.of(getForPrice(null, null, null), "1.00")
            )
        }

        @JvmStatic
        fun finalPriceValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), "0.90"),
                Arguments.of(getForFinal("1.05", "0.15", "2.1"), "0.90"),
                Arguments.of(getForFinal(null, "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", null, "0.9"), null),
                Arguments.of(getForFinal(null, null, "0.9"), null),
                Arguments.of(getForFinal(null, "0.15", null), null),
                Arguments.of(getForFinal("1.05", null, null), null),
                Arguments.of(getForFinal(null, null, null), null),
            )
        }

        @JvmStatic
        fun discountPriceValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", null, "0.9"), "0.15"),
                Arguments.of(getForFinal("1.05", "0.5", "0.9"), "0.15"),
                Arguments.of(getForFinal(null, "0.15", "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), null),
                Arguments.of(getForFinal(null, "0.15", null), null),
                Arguments.of(getForFinal(null, null, "0.9"), "0.00"),
                Arguments.of(getForFinal("1.05", null, null), "0.00"),
                Arguments.of(getForFinal(null, null, null), "0.00"),
            )
        }

        @JvmStatic
        fun subtotalPriceWithUnitAndQuantityValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForPrice("1.055", "2", "2.11"), null),
                Arguments.of(getForPrice("1.055", "2", null), "2.11"),
                Arguments.of(getForPrice("1.055", "2", "2"), "2.11"),
                Arguments.of(getForPrice(null, "2", "2"), null),
                Arguments.of(getForPrice("1.055", null, "2"), null),
                Arguments.of(getForPrice(null, null, "2"), null),
                Arguments.of(getForPrice(null, "2", null), null),
                Arguments.of(getForPrice("1.055", null, null), null),
                Arguments.of(getForPrice(null, null, null), null),
            )
        }

        @JvmStatic
        fun subtotalPriceWithFinalAndDiscountValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForFinal("1.05", "0.15", "0.9"), null),
                Arguments.of(getForFinal(null, "0.15", "0.9"), "1.05"),
                Arguments.of(getForFinal("1", "0.15", "0.9"), "1.05"),
                Arguments.of(getForFinal("1.05", null, "0.9"), null),
                Arguments.of(getForFinal("1.05", "0.15", null), null),
                Arguments.of(getForFinal("1.05", null, null), null),
                Arguments.of(getForFinal(null, null, "0.9"), null),
                Arguments.of(getForFinal(null, "0.15", null), null),
                Arguments.of(getForFinal(null, null, null), null),
            )
        }


        @JvmStatic
        fun subtotalPriceWithAllValuesProvider(): Stream<Arguments> {
            return Stream.of(

                Arguments.of(getForSubtotal("1.055", "2", "2.11", "0.21", "1.9"), null),

                Arguments.of(getForSubtotal("1.055", "2", null, "0.21", "1.9"), "2.11"),
                Arguments.of(getForSubtotal("1.055", null, "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal(null, "2", "2.11", "0.21", "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", null, "1.9"), null),
                Arguments.of(getForSubtotal("1.055", "2", "2.11", "0.21", null), null),

                Arguments.of(getForSubtotal(null, "2", "2.11", "0.21", null), null),
                Arguments.of(getForSubtotal("1.055", null, "2.11", null, "1.9"), null),
                Arguments.of(getForSubtotal("1.055", null, "2.11", "0.21", null), null),
                Arguments.of(getForSubtotal(null, "2", "2.11", null, "1.9"), null),

                Arguments.of(getForSubtotal(null, "2", null, "0.21", null), null),
                Arguments.of(getForSubtotal("1.055", null, null, null, "1.9"), null),
                Arguments.of(getForSubtotal("1.055", null, null, "0.21", null), null),
                Arguments.of(getForSubtotal(null, "2", null, null, "1.9"), null),


                )
        }

        @JvmStatic
        fun subtotalPriceWithAllValuesWithTwoSuggestionsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(getForSubtotal("1.055", "2", null, "0.5", "1.1"), "2.11", "1.60"),
                Arguments.of(getForSubtotal("1.055", "2", "0.5", "0.5", "1.1"), "2.11", "1.60"),

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
