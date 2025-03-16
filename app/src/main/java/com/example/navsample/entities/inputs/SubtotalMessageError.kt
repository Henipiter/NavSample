package com.example.navsample.entities.inputs

class SubtotalMessageError(
    var firstSuggestion: String? = null,
    var secondSuggestion: String? = null,
    var subtotalUnit: Boolean = false,
    var subtotalFinal: Boolean = false
)