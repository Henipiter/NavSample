package com.example.navsample.entities.firestore

class ProductFirebase(
    var id: String,
    var receiptId: String,
    var categoryId: String,
    var isReceiptSync: Boolean,
    var isCategorySync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity {
    override fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "receiptId" to this.receiptId,
            "categoryId" to this.categoryId,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isReceiptSync" to this.isReceiptSync,
            "isCategorySync" to this.isCategorySync,
            "isSync" to this.isSync
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
