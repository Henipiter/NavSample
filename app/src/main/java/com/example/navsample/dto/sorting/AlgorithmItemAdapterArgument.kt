package com.example.navsample.dto.sorting

import com.example.navsample.dto.Status
import com.example.navsample.dto.Type

data class AlgorithmItemAdapterArgument(
    override var value: String,
    override var type: Type,
    var number: Int,
    var status: Status,
    var empty: Boolean
) : ItemAdapterArgument(value, type) {
    constructor(value: String) : this(value, Type.UNDEFINED, -1, Status.DEFAULT, false)
    constructor(value: String, empty: Boolean) : this(
        value,
        Type.UNDEFINED,
        -1,
        Status.DEFAULT,
        empty
    )

    constructor(argument: AlgorithmItemAdapterArgument) : this(
        argument.value,
        argument.type,
        argument.number,
        argument.status,
        argument.empty
    )
}