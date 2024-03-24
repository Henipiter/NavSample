package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.Status

class OperationDelete(
    algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    namesAdapter: AlgorithmItemListAdapter,
    pricesAdapter: AlgorithmItemListAdapter
) : Operation(algorithmPrices, algorithmNames, namesAdapter, pricesAdapter) {
    override fun execute(checkedElementsCounter: Int) {
        removeItem(algorithmPrices, pricesAdapter)
        removeItem(algorithmNames, namesAdapter)
    }

    private fun removeItem(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter
    ) {
        for (i in list.lastIndex downTo 0) {
            if (list[i].status != Status.CHOSEN) {
                continue
            }
            list.removeAt(i)
            adapter.notifyItemRemoved(i)
        }
    }
}
