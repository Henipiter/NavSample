package com.example.navsample.viewmodels.fragment

import android.app.Application
import com.example.navsample.R
import com.example.navsample.dto.StringProvider
import com.example.navsample.entities.ReceiptDao
import com.example.navsample.entities.inputs.ProductFinalPriceInputs
import com.example.navsample.entities.inputs.ProductPriceInputs
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`


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
            { assertEquals(null, errors.discountError) },
            { assertEquals(null, errors.finalPriceError) },
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
}
