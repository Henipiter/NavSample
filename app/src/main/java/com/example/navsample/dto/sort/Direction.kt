package com.example.navsample.dto.sort

enum class Direction(val value: String) {
    ASCENDING("asc"), DESCENDING("desc");

    companion object {
        fun getDirection(isAscending: Boolean): Direction {
            if (isAscending) {
                return ASCENDING
            }
            return DESCENDING
        }
    }
}