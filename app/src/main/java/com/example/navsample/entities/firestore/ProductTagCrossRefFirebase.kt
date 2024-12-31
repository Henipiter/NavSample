package com.example.navsample.entities.firestore

class ProductTagCrossRefFirebase(
    var id: String,
    var isProductSync: Boolean,
    var isTagSync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity {
    override fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync,
            "isProductSync" to this.isProductSync,
            "isTagSync" to this.isTagSync,
            "isSync" to this.isSync
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
