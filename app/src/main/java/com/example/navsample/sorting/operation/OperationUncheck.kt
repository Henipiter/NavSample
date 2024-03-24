package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.Status

class OperationUncheck(
    algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    namesAdapter: AlgorithmItemListAdapter,
    pricesAdapter: AlgorithmItemListAdapter
) : Operation(algorithmPrices, algorithmNames, namesAdapter, pricesAdapter) {
    override fun execute(checkedElementsCounter: Int) {
        uncheckElement(algorithmPrices, pricesAdapter)
        uncheckElement(algorithmNames, namesAdapter)

    }

    private fun uncheckElement(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter
    ) {
        list.forEachIndexed { position, item ->
            if (item.number >= 0) {
                item.status = Status.DEFAULT
                item.number = -1
                adapter.notifyItemChanged(position)
            }
        }

    }
}
