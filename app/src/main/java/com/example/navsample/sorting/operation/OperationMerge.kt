package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type

class OperationMerge(
    algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    namesAdapter: AlgorithmItemListAdapter,
    pricesAdapter: AlgorithmItemListAdapter
) : Operation(algorithmPrices, algorithmNames, namesAdapter, pricesAdapter) {

    private var firstElement: AlgorithmItemAdapterArgument? = null
    private var firstElementPosition = -1
    private var firstElementListType = Type.UNDEFINED


    override fun execute(checkedElementsCounter: Int) {
        firstElement = null
        firstElementPosition = -1
        firstElementListType = Type.UNDEFINED

        val valueList = prepareListWithGivenSize(checkedElementsCounter)

        fillValueList(algorithmPrices, valueList) { element, index ->
            firstElement = element
            firstElementPosition = index
            firstElementListType = Type.PRICE
        }
        fillValueList(algorithmNames, valueList) { element, index ->
            firstElement = element
            firstElementPosition = index
            firstElementListType = Type.NAME
        }

        updateFirstElement(valueList)

        removeMergedElements(algorithmNames, namesAdapter)
        removeMergedElements(algorithmPrices, pricesAdapter)
    }

    private fun fillValueList(
        list: List<AlgorithmItemAdapterArgument>,
        valueList: MutableList<String>,
        firstElement: (AlgorithmItemAdapterArgument, Int) -> Unit
    ) {
        list.forEachIndexed { position, it ->
            if (it.status != Status.CHOSEN) {
                return@forEachIndexed
            }
            if (it.number == 0) {
                valueList[0] = it.value
                it.number = -1
                it.status = Status.DEFAULT
                firstElement.invoke(it, position)
            }
            if (it.number > 0) {
                valueList[it.number] = it.value
            }
        }
    }

    private fun updateFirstElement(valueList: MutableList<String>) {
        if (firstElement == null) {
            throw Exception("Can't find first element")
        }

        firstElement?.let {
            it.value = valueList.joinToString(separator = " ").replace("\\s+".toRegex(), " ")

            if (firstElementListType == Type.NAME) {
                namesAdapter.notifyItemChanged(firstElementPosition)
            }
            if (firstElementListType == Type.PRICE) {
                pricesAdapter.notifyItemChanged(firstElementPosition)
            }
        }
    }

    private fun removeMergedElements(
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter
    ) {
        for (i in list.lastIndex downTo 0) {
            if (list[i].status == Status.CHOSEN && list[i].number > 0) {
                list.removeAt(i)
                adapter.notifyItemRemoved(i)
            }
        }
    }
}
