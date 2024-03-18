package com.example.navsample.dto

data class ExperimentalAdapterArgument(
    var value: String,
    var chosen: Boolean,
    var type: Type,
    var number: Int,
) {
    constructor(value: String) : this(value, false, Type.UNDEFINED, 0)
    constructor(value: String, type: Type) : this(value, false, type, 0)
    constructor() : this("", false, Type.NAME, 0)
    constructor(argument: ExperimentalAdapterArgument) : this(
        argument.value,
        argument.chosen,
        argument.type,
        argument.number
    )
}