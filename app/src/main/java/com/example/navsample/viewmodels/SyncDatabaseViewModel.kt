package com.example.navsample.viewmodels

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.FirebaseHelperImpl
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.RoomDatabaseHelperFirebaseSync
import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.firestore.CategoryFirebase
import com.example.navsample.entities.firestore.ProductFirebase
import com.example.navsample.entities.firestore.ProductTagCrossRefFirebase
import com.example.navsample.entities.firestore.ReceiptFirebase
import com.example.navsample.entities.firestore.StoreFirebase
import com.example.navsample.entities.firestore.TagFirebase
import com.example.navsample.entities.firestore.TranslateFirebaseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass


class SyncDatabaseViewModel : ViewModel() {

    companion object {
        const val CATEGORY_LAST_UPDATE_KEY = "newestCategoryUpdateDate"
        const val STORE_LAST_UPDATE_KEY = "newestStoreUpdateDate"
        const val RECEIPT_LAST_UPDATE_KEY = "newestReceiptUpdateDate"
        const val PRODUCT_LAST_UPDATE_KEY = "newestProductUpdateDate"
        const val TAG_LAST_UPDATE_KEY = "newestTagUpdateDate"
        const val PRODUCT_TAG_LAST_UPDATE_KEY = "newestProductTagUpdateDate"
    }

    private var roomDatabaseHelper: RoomDatabaseHelper
    private var roomDatabaseHelperFirebase: RoomDatabaseHelperFirebaseSync

    private var categoryReading = false
    private var storeReading = false
    private var receiptReading = false
    private var productReading = false
    private var tagReading = false
    private var productTagReading = false
    var categoryRead = MutableLiveData(false)
    var storeRead = MutableLiveData(false)
    var receiptRead = MutableLiveData(false)
    var productRead = MutableLiveData(false)
    var tagRead = MutableLiveData(false)
    var productTagRead = MutableLiveData(false)

    var notSyncedCategoryList = MutableLiveData<List<CategoryFirebase>>()
    var notSyncedStoreList = MutableLiveData<List<StoreFirebase>>()
    var notSyncedReceiptList = MutableLiveData<List<ReceiptFirebase>>()
    var notSyncedProductList = MutableLiveData<List<ProductFirebase>>()
    var notSyncedTagList = MutableLiveData<List<TagFirebase>>()
    var notSyncedProductTagList = MutableLiveData<List<ProductTagCrossRefFirebase>>()

    var outdatedCategoryList = MutableLiveData<List<Category>>()
    var outdatedStoreList = MutableLiveData<List<Store>>()
    var outdatedReceiptList = MutableLiveData<List<Receipt>>()
    var outdatedProductList = MutableLiveData<List<Product>>()
    var outdatedTagList = MutableLiveData<List<Tag>>()
    var outdatedProductTagList = MutableLiveData<List<ProductTagCrossRef>>()

    init {
        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
        roomDatabaseHelperFirebase = RoomDatabaseHelperFirebaseSync(dao)

    }

    fun readFirestoreChanges() {
        if (isFirebaseActive()) {
            loadAllList()
            readFirebaseChanges()
        }
    }

    private fun <T : TranslateEntity> readFirebaseChangesTemplate(
        entityClass: KClass<out T>,
        preferencesKey: String
    ) {
        val date = getPreferencesKey(preferencesKey)
        val query =
            FirestoreHelperSingleton.getInstance().getDataByQuery(entityClass, date) ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val snapshot = query.get().await()
                val elements = FirestoreHelperSingleton.getInstance()
                    .convertQueryResponse(entityClass, snapshot)
                if (elements.isNotEmpty()) {
                    var isSaved = false
                    elements.forEach { entity ->
                        if (entity.isSync) {
                            isSaved = roomDatabaseHelper.saveEntityFromFirestore(entity)
                        }
                    }
                    if (isSaved) {
                        setPreferencesKey(preferencesKey, elements.last().updatedAt)
                        readFirebaseChangesTemplate(entityClass, preferencesKey)
                    } else {
                        markAsFinished(entityClass)
                    }
                } else {
                    markAsFinished(entityClass)
                }
            }
        }
    }

    private fun readFirebaseChanges() {
        if (!categoryReading) {
            categoryReading = true
            readFirebaseChangesTemplate(Category::class, CATEGORY_LAST_UPDATE_KEY)
        }
        if (!storeReading) {
            storeReading = true
            readFirebaseChangesTemplate(Store::class, STORE_LAST_UPDATE_KEY)
        }
        if (!receiptReading) {
            receiptReading = true
            readFirebaseChangesTemplate(Receipt::class, RECEIPT_LAST_UPDATE_KEY)
        }
        if (!productReading) {
            productReading = true
            readFirebaseChangesTemplate(Product::class, PRODUCT_LAST_UPDATE_KEY)
        }
        if (!tagReading) {
            tagReading = true
            readFirebaseChangesTemplate(Tag::class, TAG_LAST_UPDATE_KEY)
        }
        if (!productTagReading) {
            productTagReading = true
            readFirebaseChangesTemplate(ProductTagCrossRef::class, PRODUCT_TAG_LAST_UPDATE_KEY)
        }
    }

    private fun <T : TranslateEntity> markAsFinished(entity: KClass<out T>) {
        when (entity) {
            Category::class -> {
                categoryReading = false
                categoryRead.postValue(true)
            }

            Store::class -> {
                storeReading = false
                storeRead.postValue(true)
            }

            Receipt::class -> {
                receiptReading = false
                receiptRead.postValue(true)
            }

            Product::class -> {
                productReading = false
                productRead.postValue(true)
            }

            Tag::class -> {
                tagReading = false
                tagRead.postValue(true)
            }

            ProductTagCrossRef::class -> {
                productTagReading = false
                productTagRead.postValue(true)
            }
        }
    }

    private fun getPreferencesKey(key: String): String {
        return ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
            ?.getString(key, "")
            ?: ""
    }

    private fun setPreferencesKey(key: String, date: String) {
        val myPref = ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        myPref?.edit()?.putString(key, date)?.apply()
    }

    fun isFirebaseActive(): Boolean {
        return FirestoreHelperSingleton.getInstance() is FirebaseHelperImpl
    }

    fun clearAllList() {
        notSyncedCategoryList.postValue(listOf())
        notSyncedStoreList.postValue(listOf())
        notSyncedReceiptList.postValue(listOf())
        notSyncedProductList.postValue(listOf())
        notSyncedProductTagList.postValue(listOf())
        notSyncedTagList.postValue(listOf())

        outdatedCategoryList.postValue(listOf())
        outdatedStoreList.postValue(listOf())
        outdatedReceiptList.postValue(listOf())
        outdatedProductList.postValue(listOf())
        outdatedTagList.postValue(listOf())
        outdatedProductTagList.postValue(listOf())
    }

    fun loadNotAddedAndSync() {
        viewModelScope.launch {
            loadNotAddedList()
            delay(5000)
            loadAllList()
        }
    }

    fun loadNotAddedList() {
        loadNotAddedCategories()
        loadNotAddedStores()
        loadNotAddedReceipts()
        loadNotAddedProducts()
        loadNotAddedTags()
        loadNotAddedProductTags()
    }

    fun loadAllList() {
        if (!isFirebaseActive()) {
            return
        }
        loadNotSynced(Category::class)
        loadNotSynced(Store::class)
        loadNotSynced(Receipt::class)
        loadNotSynced(Product::class)
        loadNotSynced(Tag::class)
        loadNotSynced(ProductTagCrossRef::class)

        loadOutdated(Category::class)
        loadOutdated(Store::class)
        loadOutdated(Receipt::class)
        loadOutdated(Product::class)
        loadOutdated(Tag::class)
        loadOutdated(ProductTagCrossRef::class)
    }

    /* w listenerze sprawdzac, ze jesli isSync == false
         to czy dany klucz dokumentu wystepuje w bazie lokalnej. Jak tak to ponów próbę synchronizacji

           firestoreId usunac calkowicie

         w listenerze porownywac czasy updatedAt, deletedAt

    */
    private fun loadNotAddedCategories() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedCategories()
            list.forEach {
                addNotAddedCategory(it)
            }
        }
    }

    private fun loadNotAddedTags() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedTag()
            list.forEach {
                addNotAddedTag(it)
            }
        }
    }

    private fun loadNotAddedProductTags() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedProductTags()
            list.forEach {
                addNotAddedProductTag(it)
            }
        }
    }

    private fun loadNotAddedStores() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedStore()
            list.forEach {
                addNotAddedStore(it)
            }
        }
    }

    private fun loadNotAddedReceipts() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedReceipt()
            list.forEach {
                addNotAddedReceipt(it)
            }
        }
    }

    private fun loadNotAddedProducts() {
        viewModelScope.launch {
            val list = roomDatabaseHelperFirebase.getAllNotAddedProduct()
            list.forEach {
                addNotAddedProduct(it)
            }
        }
    }

    fun <T : TranslateEntity> loadNotSynced(entity: KClass<T>) {
        viewModelScope.launch {
            when (entity) {
                Category::class -> notSyncedCategoryList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedCategories())
                Store::class -> notSyncedStoreList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedStores())
                Receipt::class -> notSyncedReceiptList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedReceipts())
                Product::class -> notSyncedProductList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedProducts())
                Tag::class -> notSyncedTagList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedTags())
                ProductTagCrossRef::class ->
                    notSyncedProductTagList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedProductTags())
            }
        }
    }

    fun <T : TranslateEntity> loadOutdated(entity: KClass<T>) {
        viewModelScope.launch {
            when (entity) {
                Category::class ->
                    outdatedCategoryList.postValue(roomDatabaseHelperFirebase.getAllOutdatedCategories())

                Store::class -> outdatedStoreList.postValue(roomDatabaseHelperFirebase.getAllOutdatedStores())
                Receipt::class -> outdatedReceiptList.postValue(roomDatabaseHelperFirebase.getAllOutdatedReceipts())
                Product::class -> outdatedProductList.postValue(roomDatabaseHelperFirebase.getAllOutdatedProducts())
                Tag::class -> outdatedTagList.postValue(roomDatabaseHelperFirebase.getAllOutdatedTags())
                ProductTagCrossRef::class ->
                    outdatedProductTagList.postValue(roomDatabaseHelperFirebase.getAllOutdatedProductTags())

            }
        }
    }

    fun loadOutdatedStores() {
        viewModelScope.launch {
            outdatedStoreList.postValue(roomDatabaseHelperFirebase.getAllOutdatedStores())
        }
    }

    fun loadOutdatedCategories() {
        viewModelScope.launch {
            outdatedCategoryList.postValue(roomDatabaseHelperFirebase.getAllOutdatedCategories())
        }
    }

    fun loadOutdatedReceipts() {
        viewModelScope.launch {
            outdatedReceiptList.postValue(roomDatabaseHelperFirebase.getAllOutdatedReceipts())
        }
    }

    fun loadOutdatedProducts() {
        viewModelScope.launch {
            outdatedProductList.postValue(roomDatabaseHelperFirebase.getAllOutdatedProducts())
        }
    }

    fun loadOutdatedTags() {
        viewModelScope.launch {
            outdatedTagList.postValue(roomDatabaseHelperFirebase.getAllOutdatedTags())
        }
    }

    fun loadOutdatedProductTags() {
        viewModelScope.launch {
            outdatedProductTagList.postValue(roomDatabaseHelperFirebase.getAllOutdatedProductTags())
        }
    }

    private fun addNotAddedCategory(category: Category) {
        FirestoreHelperSingleton.getInstance().addFirestore(category) {
            viewModelScope.launch {
                roomDatabaseHelper.updateCategoryFirestoreId(category.id, it)
            }
        }
    }

    private fun addNotAddedStore(store: Store) {
        FirestoreHelperSingleton.getInstance().addFirestore(store) {
            viewModelScope.launch {
                roomDatabaseHelper.updateStoreFirestoreId(store.id, it)
            }
        }
    }

    private fun addNotAddedReceipt(receipt: Receipt) {
        FirestoreHelperSingleton.getInstance().addFirestore(receipt) {
            viewModelScope.launch {
                roomDatabaseHelper.updateReceiptFirestoreId(receipt.id, it)
            }
        }
    }

    private fun addNotAddedProduct(product: Product) {
        FirestoreHelperSingleton.getInstance().addFirestore(product) {
            viewModelScope.launch {
                roomDatabaseHelper.updateProductFirestoreId(product.id, it)
            }
        }
    }

    private fun addNotAddedTag(tag: Tag) {
        FirestoreHelperSingleton.getInstance().addFirestore(tag) {
            viewModelScope.launch {
                roomDatabaseHelper.updateTagFirestoreId(tag.id, it)
            }
        }
    }

    private fun addNotAddedProductTag(productTag: ProductTagCrossRef) {
        FirestoreHelperSingleton.getInstance().addFirestore(productTag) {
            viewModelScope.launch {
                roomDatabaseHelper.updateProductTagFirestoreId(productTag.id, it)
            }
        }
    }

    fun syncOutdatedCategory(category: Category) {
        if (category.isSync && category.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(category) { id ->
                viewModelScope.launch { roomDatabaseHelper.markCategoryAsUpdated(id) }
            }
        }
        if (category.isSync && category.toDelete) {
            FirestoreHelperSingleton.getInstance().delete(category) { id ->
                viewModelScope.launch { roomDatabaseHelper.markCategoryAsDeleted(id) }
            }
        }
    }

    fun syncOutdatedStore(store: Store) {
        if (store.isSync && store.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(store) { id ->
                viewModelScope.launch { roomDatabaseHelper.markStoreAsUpdated(id) }
            }
        }
        if (store.isSync && store.toDelete) {
            FirestoreHelperSingleton.getInstance().updateFirestore(store) { id ->
                viewModelScope.launch { roomDatabaseHelper.markStoreAsDeleted(id) }
            }
        }
    }

    fun syncOutdatedReceipt(receipt: Receipt) {
        if (receipt.isSync && receipt.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(receipt) { id ->
                viewModelScope.launch { roomDatabaseHelper.markCategoryAsUpdated(id) }
            }
        }
        if (receipt.isSync && receipt.toDelete) {
            FirestoreHelperSingleton.getInstance().delete(receipt) { id ->
                viewModelScope.launch { roomDatabaseHelper.markCategoryAsDeleted(id) }
            }
        }
    }

    fun syncOutdatedProduct(product: Product) {
        if (product.isSync && product.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(product) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsUpdated(id) }
            }
        }
        if (product.isSync && product.toDelete) {
            FirestoreHelperSingleton.getInstance().delete(product) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductAsDeleted(id) }
            }
        }
    }

    fun syncOutdatedTag(tag: Tag) {
        if (tag.isSync && tag.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(tag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markTagAsUpdated(id) }
            }
        }
        if (tag.isSync && tag.toDelete) {
            FirestoreHelperSingleton.getInstance().delete(tag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markTagAsDeleted(id) }
            }
        }
    }

    fun syncOutdatedProductTag(productTag: ProductTagCrossRef) {
        if (productTag.isSync && productTag.toUpdate) {
            FirestoreHelperSingleton.getInstance().updateFirestore(productTag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductTagAsUpdated(id) }
            }
        }
        if (productTag.isSync && productTag.toDelete) {
            FirestoreHelperSingleton.getInstance().delete(productTag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markProductTagAsDeleted(id) }
            }
        }
    }

    private fun categorySyncStatusOperation(category: CategoryFirebase): Boolean {
        if (category.firestoreId != "" && category.firestoreId != category.id) {
            updateCategoryFirebaseIdWithDependentStores(category.id)
        } else if (category.id == category.firestoreId) {
            category.isSync = true
            Log.i("Firebase", "Syncing category ${category}. categorySyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(category) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncCategory(id) }
            }
            return true
        }
        return false
    }

    private fun storeSyncStatusOperation(store: StoreFirebase): Boolean {
        if (store.firestoreId != "" && store.firestoreId != store.id && store.isCategorySync) {
            updateStoreFirebaseIdWithDependentReceipts(store.id)
        } else if (store.id == store.firestoreId && store.isCategorySync) {
            store.isSync = true
            Log.i("Firebase", "Syncing store $store. storeSyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(store) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncStore(id) }
            }
            return true
        }
        return false
    }

    private fun receiptSyncStatusOperation(receipt: ReceiptFirebase): Boolean {
        if (receipt.firestoreId != "" && receipt.firestoreId != receipt.id && receipt.isStoreSync) {
            updateReceiptFirebaseIdWithDependentProducts(receipt.id)
        } else if (receipt.id == receipt.firestoreId && receipt.isStoreSync) {
            receipt.isSync = true
            Log.i("Firebase", "Syncing recei[t $receipt. receiptSyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(receipt) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncReceipt(id) }
            }
            return true
        }
        return false
    }

    fun <T : TranslateFirebaseEntity> syncStatusOperation(entity: T): Boolean {
        return when (entity) {
            is CategoryFirebase -> categorySyncStatusOperation(entity)
            is StoreFirebase -> storeSyncStatusOperation(entity)
            is ReceiptFirebase -> receiptSyncStatusOperation(entity)
            is ProductFirebase -> productSyncStatusOperation(entity)
            is TagFirebase -> tagSyncStatusOperation(entity)
            is ProductTagCrossRefFirebase -> productTagSyncStatusOperation(entity)
            else -> true
        }
    }

    private fun productSyncStatusOperation(product: ProductFirebase): Boolean {
        if (product.firestoreId != "" && product.firestoreId != product.id && product.isCategorySync) {
            updateProductFirebaseId(product.id)
        } else if (product.id == product.firestoreId && product.isReceiptSync && product.isCategorySync) {
            product.isSync = true
            Log.i("Firebase", "Syncing product $product. productSyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(product) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncProduct(id) }
            }
            return true
        }
        return false
    }

    private fun tagSyncStatusOperation(tag: TagFirebase): Boolean {
        if (tag.firestoreId != "" && tag.firestoreId != tag.id) {
            updateTagFirebaseId(tag.id)
        } else if (tag.id == tag.firestoreId) {
            tag.isSync = true
            Log.i("Firebase", "Syncing tag $tag. tagSyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(tag) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncTag(id) }
            }
            return true
        }
        return false
    }

    private fun productTagSyncStatusOperation(productTag: ProductTagCrossRefFirebase): Boolean {
        if (productTag.firestoreId != "" && productTag.firestoreId != productTag.id && productTag.isProductSync && productTag.isTagSync) {
            updateProductTagFirebaseId(productTag.id)
        } else if (productTag.id == productTag.firestoreId && productTag.isProductSync && productTag.isTagSync) {
            productTag.isSync = true
            Log.i("Firebase", "Syncing product tag $productTag. productTagSyncStatusOperation")
            FirestoreHelperSingleton.getInstance().synchronize(productTag) { id ->
                viewModelScope.launch { roomDatabaseHelperFirebase.syncProductTag(id) }
            }
            return true
        }
        return false
    }

    private fun updateCategoryFirebaseIdWithDependentStores(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceCategoryWithDependencies(oldId)
            }
        }
    }

    private fun updateStoreFirebaseIdWithDependentReceipts(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceStoreWithDependencies(oldId)
            }
        }
    }

    private fun updateReceiptFirebaseIdWithDependentProducts(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceReceiptWithDependencies(oldId)
            }
        }
    }

    private fun updateProductFirebaseId(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceProductWithDependencies(oldId)
            }
        }
    }

    private fun updateTagFirebaseId(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceTagWithDependencies(oldId)
            }
        }
    }

    private fun updateProductTagFirebaseId(oldId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomDatabaseHelperFirebase.replaceProductTagWithDependencies(oldId)
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch { roomDatabaseHelper.deleteAllData() }
    }

}
