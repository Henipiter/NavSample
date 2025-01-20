package com.example.navsample.viewmodels.fragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.R
import com.example.navsample.dto.DataMode
import com.example.navsample.dto.PriceUtils.Companion.doublePriceTextToInt
import com.example.navsample.dto.PriceUtils.Companion.doubleQuantityTextToInt
import com.example.navsample.dto.PriceUtils.Companion.intPriceToString
import com.example.navsample.dto.PriceUtils.Companion.intQuantityToString
import com.example.navsample.dto.StringProvider
import com.example.navsample.dto.TagList
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDao
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.inputs.ProductErrorInputsMessage
import com.example.navsample.entities.inputs.ProductFinalPriceErrorInputsMessage
import com.example.navsample.entities.inputs.ProductFinalPriceInputValues
import com.example.navsample.entities.inputs.ProductFinalPriceInputs
import com.example.navsample.entities.inputs.ProductInputs
import com.example.navsample.entities.inputs.ProductPriceErrorInputsMessage
import com.example.navsample.entities.inputs.ProductPriceInputValues
import com.example.navsample.entities.inputs.ProductPriceInputs
import com.example.navsample.entities.relations.ProductWithTag
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AddProductDataViewModel(
    application: Application,
    receiptDao: ReceiptDao,
    private var stringProvider: StringProvider
) : AndroidViewModel(application) {

    private var roomDatabaseHelper = RoomDatabaseHelper(receiptDao)

    var inputType = AddingInputType.EMPTY.name
    var productIndex = -1
    var productId = ""
    var receiptId = ""
    var storeId = ""
    var categoryId = ""

    var pickedCategory: Category? = null
    var productInputs: Product = Product()
    var mode = DataMode.NEW

    var tagList = MutableLiveData<TagList>()
    var categoryList = MutableLiveData<List<Category>>()
    var databaseProductList = MutableLiveData<ArrayList<Product>>()
    var temporaryProductList = MutableLiveData<ArrayList<Product>>()
    var aggregatedProductList = MutableLiveData<ArrayList<Product>>()
    var receiptById = MutableLiveData<Receipt?>()
    var productById = MutableLiveData<Product?>()
    var storeById = MutableLiveData<Store?>()
    var cropImageFragmentOnStart = true

    fun aggregateProductList(): List<Product> {
        val aggregatedList = arrayListOf<Product>()
        databaseProductList.value?.let { aggregatedList.addAll(it) }
        temporaryProductList.value?.let { aggregatedList.addAll(it) }
        aggregatedProductList.postValue(aggregatedList)
        return aggregatedList
    }

    fun refreshCategoryList() {
        viewModelScope.launch {
            categoryList.postValue(roomDatabaseHelper.getAllCategories())
        }
    }


    private fun insertProductTags(productTagCrossRef: ProductTagCrossRef) {
        viewModelScope.launch {
            val savedProductTag =
                roomDatabaseHelper.insertProductTag(productTagCrossRef)
            FirestoreHelperSingleton.getInstance().addFirestore(savedProductTag) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProductTagFirestoreId(savedProductTag.id, it)
                }
            }
        }
    }

    fun refreshTagsList() {
        viewModelScope.launch {
            //TODO change handling selected/non-selected; Get it by db query
            val currentTagList = roomDatabaseHelper.getAllTags()

            val selectedTags = arrayListOf<Tag>()
            val notSelectedTags = arrayListOf<Tag>()
            if (productId.isEmpty()) {
                notSelectedTags.addAll(currentTagList)
                return@launch
            }

            val productTagIds = roomDatabaseHelper.getAllProductTags(productId).map { it.tagId }
            currentTagList.forEach { tag ->
                if (productTagIds.contains(tag.id)) {
                    selectedTags.add(tag)
                } else {
                    notSelectedTags.add(tag)
                }
            }
            tagList.postValue(TagList(selectedTags, notSelectedTags))
        }
    }

    fun refreshTagsList(tags: List<Tag>) {
        viewModelScope.launch {
            //TODO change handling selected/non-selected; Get it by db query
            val currentTagList = roomDatabaseHelper.getAllTags()

            val selectedTags = arrayListOf<Tag>()
            val notSelectedTags = arrayListOf<Tag>()

            val productTagIds = tags.map { it.id }
            currentTagList.forEach { tag ->
                if (productTagIds.contains(tag.id)) {
                    selectedTags.add(tag)
                } else {
                    notSelectedTags.add(tag)
                }
            }
            tagList.postValue(TagList(selectedTags, notSelectedTags))
        }
    }


    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val deletedProduct = roomDatabaseHelper.deleteProductById(productId)
            FirestoreHelperSingleton.getInstance().delete(deletedProduct) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
        }
    }

    fun deleteProductTags(productId: String, tagId: String) {
        viewModelScope.launch {
            val deletedProductTag = roomDatabaseHelper.deleteProductTag(productId, tagId)
            FirestoreHelperSingleton.getInstance().delete(deletedProductTag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductTagAsDeleted(id) }
            }
        }
    }

    fun getReceiptById(id: String) {
        viewModelScope.launch {
            receiptById.postValue(roomDatabaseHelper.getReceiptById(id))
        }
    }

    fun getStoreById(id: String) {
        viewModelScope.launch {
            storeById.postValue(roomDatabaseHelper.getStoreById(id))
        }
    }

    fun getProductById(id: String) {
        viewModelScope.launch {
            productById.postValue(roomDatabaseHelper.getProductById(id))
        }
    }

    fun getProductByReceiptIdWithTags(receiptId: String) {
        viewModelScope.launch {
            val productTagList = roomDatabaseHelper.getProductWithTag()
            val productList = roomDatabaseHelper.getProductsByReceiptId(receiptId) as ArrayList
            getTagsForAllProducts(productList, productTagList)
            databaseProductList.postValue(productList)
        }
    }

    private fun getTagsForAllProducts(
        productList: List<Product>, productTags: List<ProductWithTag>?
    ) {
        productList.forEach {
            val tagsList = getTagsForProductId(it.id, productTags)
            it.tagList = tagsList
            it.originalTagList = tagsList
        }
    }

    private fun getTagsForProductId(
        productId: String,
        productTags: List<ProductWithTag>?
    ): ArrayList<Tag> {
        val tags = arrayListOf<Tag>()
        productTags?.forEach {
            if (it.deletedAt.isEmpty() && it.id == productId && it.tagId != null) {
                val tag = Tag(it.tagName)
                tag.id = it.tagId!!
                tags.add(tag)
            }
        }
        return tags
    }

    fun updateSingleProduct(product: Product) {
        viewModelScope.launch {
            val updatedProduct = roomDatabaseHelper.updateProduct(product)
            changeProductTags(updatedProduct.id, product)
            if (updatedProduct.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updatedProduct) {
                    viewModelScope.launch { roomDatabaseHelper.markProductAsUpdated(product.id) }
                }
            }
        }
    }

    fun insertProducts(products: List<Product>) {
        products.forEach { product ->
            if (product.id.isEmpty()) {
                insertSingleProduct(product)
            } else {
                updateSingleProduct(product)
            }
        }
    }

    private fun insertSingleProduct(product: Product) {
        viewModelScope.launch {
            val savedProduct = roomDatabaseHelper.insertProduct(product)
            addProductTags(savedProduct.id, product)
            FirestoreHelperSingleton.getInstance().addFirestore(savedProduct) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateProductFirestoreId(savedProduct.id, it)
                }
            }
        }
    }

    private fun addProductTags(productId: String, product: Product) {
        val idToSave = product.tagList.map { it.id }
        idToSave.forEach {
            insertProductTags(ProductTagCrossRef(productId, it))
        }
    }

    private fun changeProductTags(productId: String, product: Product) {
        val originalTagIds = product.originalTagList.map { it.id }
        val currentTagIds = product.tagList.map { it.id }


        val idToSave = currentTagIds.filter { !originalTagIds.contains(it) }
        Log.d("EEAARR", "toSave $idToSave")
        val idToDelete = originalTagIds.filter { !currentTagIds.contains(it) }
        Log.d("EEAARR", "toDelete $idToDelete")

        idToSave.forEach {
            insertProductTags(ProductTagCrossRef(productId, it))
        }
        idToDelete.forEach {
            deleteProductTags(productId, it)
        }


    }

    private fun isInputsSumValid(productInputs: ProductInputs): Boolean {
        return (
                doubleQuantityTextToInt(productInputs.quantity) +
                        doublePriceTextToInt(productInputs.unitPrice) ==
                        doublePriceTextToInt(productInputs.subtotalPrice)
                ) && (
                doublePriceTextToInt(productInputs.subtotalPrice) -
                        doublePriceTextToInt(productInputs.discount) ==
                        doublePriceTextToInt(productInputs.finalPrice))


    }

    fun validateObligatoryFields(productInputs: ProductInputs): ProductErrorInputsMessage {
        val errors = ProductErrorInputsMessage()
        if (productInputs.name.isNullOrEmpty()) {
            errors.name = getEmptyValueText()
        }
        if (!isInputsSumValid(productInputs)) {
            errors.isValidPrices = "BAD"
        }
        if (productInputs.categoryId == null) {
            errors.categoryId = getEmptyValueText()
        }
        if (productInputs.ptuType.isNullOrEmpty()) {
            errors.ptuType = getEmptyValueText()
        }
        val priceErrors = validatePrices(
            ProductPriceInputs(
                productInputs.quantity,
                productInputs.unitPrice,
                productInputs.subtotalPrice
            )
        )
        val finalPriceErrors = validateFinalPrices(
            ProductFinalPriceInputs(
                productInputs.subtotalPrice,
                productInputs.discount,
                productInputs.finalPrice
            )
        )

        errors.quantity = priceErrors.quantityError
        return errors
    }

    private fun validatePricesWithOneOrNoneFilledFields(
        inputValues: ProductPriceInputValues,
        errors: ProductPriceErrorInputsMessage
    ): ProductPriceErrorInputsMessage {
        if (inputValues.quantityValue == 0) {
            errors.quantitySuggestion = getSuggestionMessage("1")
        } else {
            errors.quantitySuggestion = null
        }
        return errors
    }

    private fun validatePricesWithTwoFilledFields(
        inputValues: ProductPriceInputValues,
        errors: ProductPriceErrorInputsMessage
    ): ProductPriceErrorInputsMessage {
        if (inputValues.isSubtotalPriceBlank) {
            errors.subtotalPriceSuggestion =
                getSuggestionMessage(intPriceToString(roundInt(inputValues.unitPriceValue * inputValues.quantityValue)))
        }
        if (inputValues.isUnitPriceBlank) {
            val unitPriceMessage =
                if (inputValues.quantityValue != 0) intPriceToString(inputValues.subtotalPriceValue / inputValues.quantityValue) else "100"
            errors.unitPriceSuggestion = getSuggestionMessage(unitPriceMessage)
        }
        if (inputValues.isQuantityBlank) {
            val unitQuantityMessage =
                if (inputValues.unitPriceValue != 0) intQuantityToString(inputValues.subtotalPriceValue / inputValues.unitPriceValue) else "1000"
            errors.quantitySuggestion = getSuggestionMessage(unitQuantityMessage)
        }
        return errors
    }

    private fun validatePricesWithThreeFilledFields(
        inputValues: ProductPriceInputValues,
        errors: ProductPriceErrorInputsMessage
    ): ProductPriceErrorInputsMessage {
        if (roundInt(inputValues.unitPriceValue * inputValues.quantityValue) * 1000 != inputValues.subtotalPriceValue) {
            errors.subtotalPriceSuggestion =
                getSuggestionMessage(intPriceToString(roundInt(inputValues.unitPriceValue * inputValues.quantityValue)))

            val unitPriceMessage = if (inputValues.quantityValue != 0) {
                intPriceToString(inputValues.subtotalPriceValue / inputValues.quantityValue)
            } else {
                "100"
            }
            errors.unitPriceSuggestion = getSuggestionMessage(unitPriceMessage)

            val unitQuantityMessage = if (inputValues.quantityValue != 0) {
                intQuantityToString(inputValues.subtotalPriceValue / inputValues.unitPriceValue)
            } else {
                "1000"
            }
            errors.quantitySuggestion = getSuggestionMessage(unitQuantityMessage)
        }
        return errors
    }

    private fun getPriceInputValues(productInputs: ProductPriceInputs): ProductPriceInputValues {
        val inputs = ProductPriceInputValues()

        inputs.isSubtotalPriceBlank = isNullOrBlankOrZero(productInputs.subtotalPrice)
        inputs.isUnitPriceBlank = isNullOrBlankOrZero(productInputs.unitPrice)
        inputs.isQuantityBlank = isNullOrBlankOrZero(productInputs.quantity)

        if (!inputs.isSubtotalPriceBlank) {
            inputs.subtotalPriceValue = doublePriceTextToInt(productInputs.subtotalPrice) * 1000
        }
        if (!inputs.isUnitPriceBlank) {
            inputs.unitPriceValue = doublePriceTextToInt(productInputs.unitPrice)
        }
        if (!inputs.isQuantityBlank) {
            inputs.quantityValue = doubleQuantityTextToInt(productInputs.quantity)
        }
        return inputs

    }

    private fun getFinalPriceInputValues(productInputs: ProductFinalPriceInputs): ProductFinalPriceInputValues {
        val inputs = ProductFinalPriceInputValues()

        inputs.isDiscountPriceBlank = productInputs.discount?.isBlank() ?: true
        inputs.isSubtotalPriceBlank = isNullOrBlankOrZero(productInputs.subtotalPrice)
        inputs.isFinalPriceBlank = isNullOrBlankOrZero(productInputs.finalPrize)

        if (!inputs.isSubtotalPriceBlank) {
            inputs.subtotalPriceValue = doublePriceTextToInt(productInputs.subtotalPrice)
        }
        if (!inputs.isDiscountPriceBlank) {
            inputs.discountValue = doublePriceTextToInt(productInputs.discount)
        }
        if (!inputs.isFinalPriceBlank) {
            inputs.finalPriceValue = doublePriceTextToInt(productInputs.finalPrize)
        }
        return inputs

    }

    private fun setPriceInputErrors(inputValues: ProductPriceInputValues): ProductPriceErrorInputsMessage {
        val errors = ProductPriceErrorInputsMessage()
        if (inputValues.isSubtotalPriceBlank) {
            errors.subtotalPriceError = " "
        }
        if (inputValues.isUnitPriceBlank) {
            errors.unitPriceError = " "
        }
        if (inputValues.isQuantityBlank) {
            errors.quantityError = " "
        }
        return errors
    }

    private fun setFinalPriceInputErrors(inputValues: ProductFinalPriceInputValues): ProductFinalPriceErrorInputsMessage {
        val errors = ProductFinalPriceErrorInputsMessage()
        if (inputValues.isSubtotalPriceBlank) {
            errors.subtotalPriceError = " "
        }
        if (inputValues.isDiscountPriceBlank) {
            errors.discountError = " "
        }
        if (inputValues.isFinalPriceBlank) {
            errors.finalPriceError = " "
        }
        return errors
    }

    fun validatePrices(productInputs: ProductPriceInputs): ProductPriceErrorInputsMessage {
        val inputValues = getPriceInputValues(productInputs)
        val errors = setPriceInputErrors(inputValues)

        return when (inputValues.countFill()) {
            3 -> {
                validatePricesWithThreeFilledFields(inputValues, errors)
            }

            2 -> {
                validatePricesWithTwoFilledFields(inputValues, errors)
            }

            else -> {
                validatePricesWithOneOrNoneFilledFields(inputValues, errors)
            }
        }

    }

    fun validateFinalPrices(productInputs: ProductFinalPriceInputs): ProductFinalPriceErrorInputsMessage {
        val inputValues = getFinalPriceInputValues(productInputs)
        val errors = setFinalPriceInputErrors(inputValues)

        if (inputValues.isAllFieldsFilled()) {
            if (inputValues.subtotalPriceValue - inputValues.discountValue != inputValues.finalPriceValue) {
                errors.discountError = " "
                errors.discountSuggestion =
                    getSuggestionMessage(intPriceToString(inputValues.subtotalPriceValue - inputValues.finalPriceValue))
                errors.finalPriceError = " "
                errors.finalPriceSuggestion =
                    getSuggestionMessage(intPriceToString(inputValues.subtotalPriceValue - inputValues.discountValue))
            }
        } else if (inputValues.isOnlyDiscountEmpty()) {
            errors.discountError = " "
            errors.discountSuggestion =
                getSuggestionMessage(intPriceToString(inputValues.subtotalPriceValue - inputValues.finalPriceValue))
        } else if (inputValues.isOnlyFinalPriceEmpty()) {
            errors.finalPriceError = " "
            errors.finalPriceSuggestion =
                getSuggestionMessage(intPriceToString(inputValues.subtotalPriceValue - inputValues.discountValue))
        } else if (inputValues.isDiscountAndFinalPriceEmpty()) {
            errors.discountError = " "
            errors.discountSuggestion = getSuggestionMessage("0.00")
        }
        return errors

    }

    private fun roundInt(integer: Int): Int {
        return (integer / 1000.0).roundToInt()
    }

    private fun isNullOrBlankOrZero(price: CharSequence?): Boolean {
        if (price == null) {
            return true
        }
        return price.isBlank() || price.toString().toDouble() == 0.0
    }

    private fun getEmptyValueText(): String {
        return stringProvider.getString(R.string.empty_value_error)
    }

    private fun getWrongValueText(): String {
        return stringProvider.getString(R.string.bad_value_error)
    }

    fun getSuggestionMessage(value: String): String {
        return "${getSuggestionPrefix()} $value"
    }

    fun getSuggestionPrefix(): String {
        return stringProvider.getString(R.string.suggestion_prefix)
    }
}
