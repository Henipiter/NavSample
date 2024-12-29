package com.example.navsample.dto

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DateUtil {

    companion object {
        fun getCurrentUtcTime(): String {
            val currentTime = LocalDateTime.now(ZoneOffset.UTC)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            return currentTime.format(formatter)
        }
    }
}