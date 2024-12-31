package com.example.navsample.entities.firestore

class StoreFirebase(
    var id: String,
    var defaultCategoryId: String,
    var isCategorySync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity {
    override fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "defaultCategoryId" to this.defaultCategoryId,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isCategorySync" to this.isCategorySync,
            "isSync" to this.isSync
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
