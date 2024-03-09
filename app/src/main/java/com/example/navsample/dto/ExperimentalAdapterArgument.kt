package com.example.navsample.dto

data class ExperimentalAdapterArgument(
    var value: String,
    var chosen: Boolean,
    var number: Int,
) {
    constructor(value: String) : this(value, false, 0)
    constructor() : this("", false, 0)
    constructor(argument: ExperimentalAdapterArgument) : this(
        argument.value,
        argument.chosen,
        argument.number
    )
}