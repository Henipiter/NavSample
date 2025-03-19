package com.example.navsample.entities.inputs

class TagErrorInputsMessage(
    var name: String? = null
) {
    fun isCorrect(): Boolean {
        return name == null
    }
}