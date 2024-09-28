package com.example.navsample.dto.sorting

import com.example.navsample.dto.Status
import com.example.navsample.dto.Type

data class AlgorithmItemAdapterArgument(
    override var value: String,
    override var type: Type,
    var number: Int,
    var status: Status
) : ItemAdapterArgument(value, type) {
    constructor() : this("")
    constructor(value: String) : this(value, Type.UNDEFINED, -1, Status.DEFAULT)

    constructor(argument: AlgorithmItemAdapterArgument) : this(
        argument.value,
        argument.type,
        argument.number,
        argument.status
    )
}