package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.Status

class ElementOperationHelper(
    private val algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    private val algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    private val namesAdapter: AlgorithmItemListAdapter,
    private val pricesAdapter: AlgorithmItemListAdapter
) {
    private var checkedElementsCounter = 0

    fun checkAndUncheckSingleElement(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        if (list[position].status == Status.DEFAULT) {
            list[position].status = Status.CHOSEN
            list[position].number = checkedElementsCounter
            checkedElementsCounter += 1
        } else if (list[position].status == Status.CHOSEN) {
            list[position].status = Status.DEFAULT
            decrementNumberInCell(list[position].number, algorithmPrices, pricesAdapter)
            decrementNumberInCell(list[position].number, algorithmNames, namesAdapter)
            list[position].number = 0
            checkedElementsCounter -= 1
        }
    }

    private fun decrementNumberInCell(
        limit: Int,
        list: List<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter
    ) {
        list.forEachIndexed { position, item ->
            if (item.number > limit) {
                item.number -= 1
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun executeElementOperation(action: SortingElementAction) {
        if (checkedElementsCounter == 0) {
            return
        }
        val operation = when (action) {
            SortingElementAction.DELETE -> OperationDelete(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )

            SortingElementAction.SWAP -> OperationSwap(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )

            SortingElementAction.CLEAR_VALUE -> OperationClearValue(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )

            SortingElementAction.CLEAR_STATUS -> OperationClearStatus(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )

            SortingElementAction.MERGE -> OperationMerge(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )

            SortingElementAction.UNCHECK -> OperationUncheck(
                algorithmPrices,
                algorithmNames,
                namesAdapter,
                pricesAdapter
            )
        }
        operation.execute(checkedElementsCounter)
        checkedElementsCounter = 0
    }
}
