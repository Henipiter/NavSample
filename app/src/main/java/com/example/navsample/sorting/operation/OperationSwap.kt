package com.example.navsample.sorting.operation

import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.dto.Status
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument

class OperationSwap(
    algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    namesAdapter: AlgorithmItemListAdapter,
    pricesAdapter: AlgorithmItemListAdapter
) : Operation(algorithmPrices, algorithmNames, namesAdapter, pricesAdapter) {
    private var checkedElements = 0

    override fun execute(checkedElementsCounter: Int) {
        this.checkedElements = checkedElementsCounter
        val valueList = prepareListWithGivenSize(checkedElements)

        readValues(algorithmPrices, valueList)
        readValues(algorithmNames, valueList)

        swapElements(algorithmNames, namesAdapter, valueList)
        swapElements(algorithmPrices, pricesAdapter, valueList)
    }

    private fun readValues(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        valueList: MutableList<String>
    ) {
        list.forEach {
            if (it.status != Status.CHOSEN) {
                return@forEach
            }
            valueList[it.number] = it.value
        }
    }

    private fun swapElements(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter,
        valueList: MutableList<String>
    ) {
        list.forEachIndexed { position, it ->
            if (it.status != Status.CHOSEN) {
                return@forEachIndexed
            }
            if (isLastElementOdd(it)) {
                it.number = -1
                it.status = Status.DEFAULT
                adapter.notifyItemChanged(position)
            } else if (it.number >= 0) {
                val indexOfSecondItemFromPair = getIndexOfPair(it.number)
                it.value = valueList[indexOfSecondItemFromPair]
                it.number = -1
                it.status = Status.DEFAULT
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun getIndexOfPair(index: Int): Int {
        return if (index % 2 == 0) {
            index + 1
        } else {
            index - 1
        }
    }

    private fun isLastElementOdd(it: AlgorithmItemAdapterArgument): Boolean {
        return checkedElements % 2 == 1 && it.number == checkedElements - 1

    }
}
