package com.example.navsample.dto

data class AlgorithmItemAdapterArgument(
    var value: String,
    var status: Status,
    var type: Type,
    var number: Int,
) {
    constructor(value: String) : this(value, Status.DEFAULT, Type.UNDEFINED, -1)
    constructor(value: String, type: Type) : this(value, Status.DEFAULT, type, -1)
    constructor() : this("", Status.DEFAULT, Type.NAME, -1)
    constructor(argument: AlgorithmItemAdapterArgument) : this(
        argument.value,
        argument.status,
        argument.type,
        argument.number
    )
}