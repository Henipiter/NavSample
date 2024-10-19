package com.example.navsample.dto.inputmode

enum class AddingInputType {
    ID, FIELD, EMPTY, INDEX;

    companion object {
        fun getByName(name: String): AddingInputType {
            return when (name) {
                "ID" -> ID
                "FIELD" -> FIELD
                "INDEX" -> INDEX
                else -> EMPTY
            }
        }
    }
}