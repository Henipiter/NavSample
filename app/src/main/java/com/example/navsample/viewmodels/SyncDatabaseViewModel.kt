package com.example.navsample.viewmodels

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelperImpl
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.RoomDatabaseHelperFirebaseSync
import com.example.navsample.entities.Store
import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.dto.CategoryFirebase
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.reflect.KClass


class SyncDatabaseViewModel : ViewModel() {

    companion object {
        const val CATEGORY_LAST_UPDATE_KEY = "newestCategoryUpdateDate"
        const val STORE_LAST_UPDATE_KEY = "newestStoreUpdateDate"
        const val RECEIPT_LAST_UPDATE_KEY = "newestReceiptUpdateDate"
        const val PRODUCT_LAST_UPDATE_KEY = "newestProductUpdateDate"
    }

    private var roomDatabaseHelper: RoomDatabaseHelper
    private var roomDatabaseHelperFirebase: RoomDatabaseHelperFirebaseSync

    private var categoryReading = false
    private var storeReading = false
    private var receiptReading = false
    private var productReading = false
    var categoryRead = MutableLiveData(false)
    var storeRead = MutableLiveData(false)
    var receiptRead = MutableLiveData(false)
    var productRead = MutableLiveData(false)

    var notSyncedProductList = MutableLiveData<List<ProductFirebase>>()
    var notSyncedReceiptList = MutableLiveData<List<ReceiptFirebase>>()
    var notSyncedCategoryList = MutableLiveData<List<CategoryFirebase>>()
    var notSyncedStoreList = MutableLiveData<List<StoreFirebase>>()

    var outdatedProductList = MutableLiveData<List<Product>>()
    var outdatedReceiptList = MutableLiveData<List<Receipt>>()
    var outdatedCategoryList = MutableLiveData<List<Category>>()
    var outdatedStoreList = MutableLiveData<List<Store>>()

    init {
        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
        roomDatabaseHelperFirebase = RoomDatabaseHelperFirebaseSync(dao)

    }

    fun readFirestoreChanges() {
        if (isFirebaseActive()) {
            loadAllList()
            readCategoryFirebaseChanges()
        }
    }

    private fun <T : TranslateEntity> readFirebaseChangesTemplate(
        entityClass: KClass<out T>,
        preferencesKey: String
    ) {
        val temp = UUID.randomUUID().toString()
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
                        isSaved = roomDatabaseHelper.saveEntityFromFirestore(entity)
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

    private fun readCategoryFirebaseChanges() {
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
        notSyncedProductList.postValue(listOf())
        notSyncedReceiptList.postValue(listOf())
        notSyncedCategoryList.postValue(listOf())
        notSyncedStoreList.postValue(listOf())
        outdatedProductList.postValue(listOf())
        outdatedReceiptList.postValue(listOf())
        outdatedCategoryList.postValue(listOf())
        outdatedStoreList.postValue(listOf())
    }

    fun loadNotAddedList() {
        loadNotAddedCategories()
        loadNotAddedStores()
        loadNotAddedReceipts()
        loadNotAddedProducts()
    }

    fun loadAllList() {
        if (!isFirebaseActive()) {
            return
        }
        loadNotSyncedStores()
        loadNotSyncedReceipts()
        loadNotSyncedProducts()
        loadNotSyncedCategories()
        loadOutdatedStores()
        loadOutdatedReceipts()
        loadOutdatedProducts()
        loadOutdatedCategories()
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

    fun loadNotSyncedStores() {
        viewModelScope.launch {
            notSyncedStoreList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedStores())
        }
    }

    fun loadNotSyncedCategories() {
        viewModelScope.launch {
            notSyncedCategoryList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedCategories())
        }
    }

    fun loadNotSyncedReceipts() {
        viewModelScope.launch {
            notSyncedReceiptList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedReceipts())
        }
    }

    fun loadNotSyncedProducts() {
        viewModelScope.launch {
            notSyncedProductList.postValue(roomDatabaseHelperFirebase.getAllNotSyncedProducts())
        }
    }

    private fun loadOutdatedStores() {
        viewModelScope.launch {
            outdatedStoreList.postValue(roomDatabaseHelperFirebase.getAllOutdatedStores())
        }
    }

    private fun loadOutdatedCategories() {
        viewModelScope.launch {
            outdatedCategoryList.postValue(roomDatabaseHelperFirebase.getAllOutdatedCategories())
        }
    }

    private fun loadOutdatedReceipts() {
        viewModelScope.launch {
            outdatedReceiptList.postValue(roomDatabaseHelperFirebase.getAllOutdatedReceipts())
        }
    }

    private fun loadOutdatedProducts() {
        viewModelScope.launch {
            outdatedProductList.postValue(roomDatabaseHelperFirebase.getAllOutdatedProducts())
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

    fun categorySyncStatusOperation(category: CategoryFirebase): Boolean {
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

    fun storeSyncStatusOperation(store: StoreFirebase): Boolean {
        if (store.firestoreId != "" && store.firestoreId != store.id) {
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

    fun receiptSyncStatusOperation(receipt: ReceiptFirebase): Boolean {
        if (receipt.firestoreId != "" && receipt.firestoreId != receipt.id) {
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

    fun productSyncStatusOperation(product: ProductFirebase): Boolean {
        if (product.firestoreId != "" && product.firestoreId != product.id) {
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

    fun deleteAllData() {
        viewModelScope.launch { roomDatabaseHelper.deleteAllData() }
    }

}
