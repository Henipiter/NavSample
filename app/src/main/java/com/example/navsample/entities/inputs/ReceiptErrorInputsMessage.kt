package com.example.navsample.entities.inputs

class ReceiptErrorInputsMessage(
    var storeId: String? = null,
    var pln: String? = null,
    var ptu: String? = null,
    var date: String? = null,
    var time: String? = null
) {
    fun isCorrect(): Boolean {
        return storeId == null &&
                pln == null &&
                ptu == null &&
                date == null &&
                time == null
    }
}