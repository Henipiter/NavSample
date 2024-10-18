package com.example.navsample.dto.inputmode

enum class AddingInputType {
    ID, FIELD, EMPTY;

    companion object {
        fun getByName(name: String): AddingInputType {
            return when (name) {
                "ID" -> ID
                "FIELD" -> FIELD
                else -> EMPTY
            }
        }
    }
}