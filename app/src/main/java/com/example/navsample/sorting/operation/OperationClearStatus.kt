package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type

class OperationClearStatus(
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
        list.forEachIndexed { position, item ->
            item.status = Status.DEFAULT
            item.number = -1
            item.type = Type.UNDEFINED
            adapter.notifyItemChanged(position)

        }

    }
}
