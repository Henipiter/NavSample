package com.example.navsample.entities.inputs

class CategoryErrorInputsMessage(
    var name: String? = null,
    var color: String? = null
) {
    fun isCorrect(): Boolean {
        return name == null &&
                color == null
    }
}