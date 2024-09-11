package com.example.navsample.sorting.operation

import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument

abstract class Operation(
    var algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    var algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    var namesAdapter: AlgorithmItemListAdapter,
    var pricesAdapter: AlgorithmItemListAdapter
) {
    abstract fun execute(checkedElementsCounter: Int)

    companion object {
        fun prepareListWithGivenSize(size: Int): MutableList<String> {
            val list = mutableListOf<String>()
            for (i in 1..size) {
                list.add("")
            }
            return list
        }
    }
}
