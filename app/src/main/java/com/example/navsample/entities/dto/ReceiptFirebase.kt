package com.example.navsample.entities.dto

class ReceiptFirebase(
    var id: String,
    var storeId: String,
    var isStoreSync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity {

    override fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "storeId" to this.storeId,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isStoreSync" to this.isStoreSync,
            "isSync" to this.isSync
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
