package com.example.navsample.dto

class NipValidator {
    companion object {
        fun validate(nip: CharSequence?): Boolean {
            if (nip == null || !Regex("""[0-9]{10}""").matches(nip)) {
                return false
            }
            val weights = arrayOf(6, 5, 7, 2, 3, 4, 5, 6, 7)
            var digitsSum = 0
            for (i in 0..8) {
                digitsSum += nip[i].digitToInt() * weights[i]
            }
            return digitsSum % 11 == nip[9].digitToInt()
        }
    }
}