package com.example.navsample.entities.inputs

class StoreErrorInputsMessage(
    var name: String? = null,
    var nip: String? = null,
    var categoryId: String? = null
) {
    fun isCorrect(): Boolean {
        return name == null &&
                nip == null &&
                categoryId == null
    }
}