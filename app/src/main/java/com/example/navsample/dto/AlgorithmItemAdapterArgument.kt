package com.example.navsample.dto

data class AlgorithmItemAdapterArgument(
    var value: String,
    var type: Type,
    var number: Int,
    var status: Status
) {
    constructor(value: String) : this(value, Type.UNDEFINED, -1, Status.DEFAULT)
    constructor(argument: AlgorithmItemAdapterArgument) : this(
        argument.value,
        argument.type,
        argument.number,
        argument.status
    )
}