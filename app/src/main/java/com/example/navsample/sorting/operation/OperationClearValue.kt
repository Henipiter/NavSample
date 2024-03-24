package com.example.navsample.sorting.operation

import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.dto.Status
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument

class OperationClearValue(
    algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    namesAdapter: AlgorithmItemListAdapter,
    pricesAdapter: AlgorithmItemListAdapter
) : Operation(algorithmPrices, algorithmNames, namesAdapter, pricesAdapter) {
    override fun execute(checkedElementsCounter: Int) {
        clearElement(algorithmPrices, pricesAdapter)
        clearElement(algorithmNames, namesAdapter)
    }

    private fun clearElement(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter
    ) {
        list.forEachIndexed { position, it ->
            if (it.status != Status.CHOSEN) {
                return@forEachIndexed
            }
            it.value = ""
            it.number = -1
            it.status = Status.DEFAULT
            adapter.notifyItemChanged(position)

        }

    }
}
