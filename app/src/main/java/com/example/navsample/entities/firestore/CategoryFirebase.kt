package com.example.navsample.entities.firestore

class CategoryFirebase(
    var id: String,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity {
    override fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
