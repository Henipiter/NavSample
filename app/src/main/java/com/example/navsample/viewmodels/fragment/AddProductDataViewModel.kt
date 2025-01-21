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
import com.example.navsample.entities.inputs.ProductInputs
import com.example.navsample.entities.inputs.SubtotalMessageError
import com.example.navsample.entities.relations.ProductWithTag
import kotlinx.coroutines.launch

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

    fun validateObligatoryFields(productInputs: ProductInputs): ProductErrorInputsMessage {
        val errors = ProductErrorInputsMessage()
        if (productInputs.name.isNullOrEmpty()) {
            errors.name = getEmptyValueText()
        }
//        if (!isInputsSumValid(productInputs)) {
//            errors.isValidPrices = "BAD"
//        }
        if (productInputs.categoryId == null) {
            errors.categoryId = getEmptyValueText()
        }
        if (productInputs.ptuType.isNullOrEmpty()) {
            errors.ptuType = getEmptyValueText()
        }

        val pricesErrors = validateAllPrices(productInputs)
        errors.quantity = pricesErrors.quantity
        errors.unitPrice = pricesErrors.unitPrice
        errors.subtotalPriceFirst = pricesErrors.subtotalPriceFirst
        errors.subtotalPriceSecond = pricesErrors.subtotalPriceSecond
        errors.discount = pricesErrors.discount
        errors.finalPrice = pricesErrors.finalPrice
        return errors
    }


    private fun getPriceValueOrNull(priceText: CharSequence?): Int? {
        val isPriceBlank1 = isNullOrBlankOrZero(priceText)
        if (!isPriceBlank1) {
            return doublePriceTextToInt(priceText)
        }
        return null
    }

    private fun getQuantityValueOrNull(priceText: CharSequence?): Int? {
        val isPriceBlank1 = isNullOrBlankOrZero(priceText)
        if (!isPriceBlank1) {
            return doubleQuantityTextToInt(priceText)
        }
        return null
    }


    fun validateAllPrices(productInputs: ProductInputs): ProductErrorInputsMessage {
        val quantityErrorMessage = validateQuantity(productInputs)
        val unitPriceErrorMessage = validateUnitPrice(productInputs)
        val subtotalErrorMessage = validateSubtotalPrice(productInputs)
        val discountErrorMessage = validateDiscount(productInputs)
        val finalPriceErrorMessage = validateFinalPrice(productInputs)

        val errors = ProductErrorInputsMessage()
        errors.quantity = quantityErrorMessage
        errors.unitPrice = unitPriceErrorMessage
        errors.subtotalPriceFirst = subtotalErrorMessage.firstSuggestion
        errors.subtotalPriceSecond = subtotalErrorMessage.secondSuggestion
        errors.discount = discountErrorMessage
        errors.finalPrice = finalPriceErrorMessage
        return errors
    }

    fun validatePrices(productInputs: ProductInputs): ProductErrorInputsMessage {
        val quantityErrorMessage = validateQuantity(productInputs)
        val unitPriceErrorMessage = validateUnitPrice(productInputs)
        val subtotalErrorMessage = validateSubtotalPrice(productInputs)

        val errors = ProductErrorInputsMessage()
        errors.quantity = quantityErrorMessage
        errors.unitPrice = unitPriceErrorMessage
        errors.subtotalPriceFirst = subtotalErrorMessage.firstSuggestion
        errors.subtotalPriceSecond = subtotalErrorMessage.secondSuggestion
        return errors
    }

    fun validateFinalPrices(productInputs: ProductInputs): ProductErrorInputsMessage {
        val subtotalErrorMessage = validateSubtotalPrice(productInputs)
        val discountErrorMessage = validateDiscount(productInputs)
        val finalPriceErrorMessage = validateFinalPrice(productInputs)

        val errors = ProductErrorInputsMessage()
        errors.subtotalPriceFirst = subtotalErrorMessage.firstSuggestion
        errors.subtotalPriceSecond = subtotalErrorMessage.secondSuggestion
        errors.discount = discountErrorMessage
        errors.finalPrice = finalPriceErrorMessage
        return errors
    }

    fun validateQuantity(productInputs: ProductInputs): String? {
        val unitPrice = getPriceValueOrNull(productInputs.unitPrice)
        val subtotalPrice = getPriceValueOrNull(productInputs.subtotalPrice)
        val quantity = getQuantityValueOrNull(productInputs.quantity)

        if (unitPrice != null && subtotalPrice != null) {
            val calculatedQuantity = subtotalPrice * 1000 / unitPrice
            if (quantity != calculatedQuantity) {
                return getSuggestionMessage(intQuantityToString(calculatedQuantity))
            }
        } else if (quantity == null) {
            return getSuggestionMessage("1.000")
        }
        return null
    }

    fun validateUnitPrice(productInputs: ProductInputs): String? {
        val quantity = getQuantityValueOrNull(productInputs.quantity)
        val subtotalPrice = getPriceValueOrNull(productInputs.subtotalPrice)
        val unitPrice = getPriceValueOrNull(productInputs.unitPrice)

        if (quantity != null && subtotalPrice != null) {
            val calculatedUnitPrice = subtotalPrice * 1000 / quantity
            if (unitPrice != calculatedUnitPrice) {
                return getSuggestionMessage(intPriceToString(calculatedUnitPrice))
            }
        } else if (unitPrice == null) {
            return getSuggestionMessage("1.00")
        }
        return null
    }

    fun validateDiscount(productInputs: ProductInputs): String? {
        val subtotalPrice = getPriceValueOrNull(productInputs.subtotalPrice)
        val finalPrice = getPriceValueOrNull(productInputs.finalPrice)
        val discount = getPriceValueOrNull(productInputs.discount)

        if (subtotalPrice != null && finalPrice != null) {
            val calculatedUnitPrice = subtotalPrice - finalPrice
            if (discount != calculatedUnitPrice) {
                return getSuggestionMessage(intPriceToString(calculatedUnitPrice))
            }
        } else if (discount == null) {
            return getSuggestionMessage("0.00")
        }
        return null
    }

    fun validateFinalPrice(productInputs: ProductInputs): String? {
        val subtotalPrice = getPriceValueOrNull(productInputs.subtotalPrice)
        val discount = getPriceValueOrNull(productInputs.discount)
        val finalPrice = getPriceValueOrNull(productInputs.finalPrice)

        if (subtotalPrice != null && discount != null) {
            val calculatedUnitPrice = subtotalPrice - discount
            if (finalPrice != calculatedUnitPrice) {
                return getSuggestionMessage(intPriceToString(calculatedUnitPrice))
            }
        } else if (finalPrice == null) {
            return getEmptyValueText()
        }
        return null
    }

    fun validateSubtotalPrice(productInputs: ProductInputs): SubtotalMessageError {
        val quantity = getQuantityValueOrNull(productInputs.quantity)
        val unitPrice = getPriceValueOrNull(productInputs.unitPrice)
        val discount = getPriceValueOrNull(productInputs.discount)
        val finalPrice = getPriceValueOrNull(productInputs.finalPrice)
        val subtotalPrice = getPriceValueOrNull(productInputs.subtotalPrice)

        var calculatedSubtotalPrice: Int? = null
        var calculatedSubtotalPriceFromFinal: Int? = null

        if (quantity != null && unitPrice != null) {
            calculatedSubtotalPrice = quantity * unitPrice / 1000
            if (subtotalPrice == calculatedSubtotalPrice) {
                calculatedSubtotalPrice = null
            }
        }

        if (discount != null && finalPrice != null) {
            calculatedSubtotalPriceFromFinal = finalPrice + discount
            if (subtotalPrice == calculatedSubtotalPriceFromFinal) {
                calculatedSubtotalPriceFromFinal = null
            }
        }
        return createSubtotalMessageError(
            calculatedSubtotalPrice,
            calculatedSubtotalPriceFromFinal,
            subtotalPrice
        )
    }

    private fun getErrorMessageWhenTwoCalculatedSubtotals(
        calculatedSubtotalPrice: Int,
        calculatedSubtotalPriceFromFinal: Int,
        errorMessage: SubtotalMessageError
    ) {
        if (calculatedSubtotalPrice == calculatedSubtotalPriceFromFinal) {
            errorMessage.firstSuggestion =
                getSuggestionMessage(intPriceToString(calculatedSubtotalPrice))
        } else {
            errorMessage.firstSuggestion =
                getSuggestionMessage(intPriceToString(calculatedSubtotalPrice))
            errorMessage.secondSuggestion =
                getSecondSuggestionMessage(intPriceToString(calculatedSubtotalPriceFromFinal))
        }
    }

    private fun createSubtotalMessageError(
        calculatedSubtotalPrice: Int?,
        calculatedSubtotalPriceFromFinal: Int?,
        subtotalPrice: Int?
    ): SubtotalMessageError {
        val errorMessage = SubtotalMessageError()
        if (calculatedSubtotalPrice != null && calculatedSubtotalPriceFromFinal != null) {
            getErrorMessageWhenTwoCalculatedSubtotals(
                calculatedSubtotalPrice,
                calculatedSubtotalPriceFromFinal,
                errorMessage
            )
        } else if (calculatedSubtotalPrice != null) {
            errorMessage.firstSuggestion =
                getSuggestionMessage(intPriceToString(calculatedSubtotalPrice))
        } else if (calculatedSubtotalPriceFromFinal != null) {
            errorMessage.firstSuggestion =
                getSuggestionMessage(intPriceToString(calculatedSubtotalPriceFromFinal))
        } else {
            if (subtotalPrice == null) {
                errorMessage.firstSuggestion = getEmptyValueText()
            }
        }
        return errorMessage
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

    fun getSuggestionMessage(value: String): String {
        return "${getSuggestionPrefix()} $value"
    }

    private fun getSecondSuggestionMessage(value: String): String {
        return "${getSecondSuggestionPrefix()} $value"
    }

    fun getSuggestionPrefix(): String {
        return stringProvider.getString(R.string.suggestion_prefix)
    }

    private fun getSecondSuggestionPrefix(): String {
        return stringProvider.getString(R.string.second_suggestion_prefix)
    }
}
