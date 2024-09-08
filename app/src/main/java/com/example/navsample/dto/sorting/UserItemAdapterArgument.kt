package com.example.navsample.dto.sorting

import com.example.navsample.dto.Type

data class UserItemAdapterArgument(
    override var value: String,
    override var type: Type,
    var algorithmItem: AlgorithmItemAdapterArgument
) : ItemAdapterArgument(value, type)