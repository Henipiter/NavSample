package com.example.navsample.sorting.operation

import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.adapters.UserItemListAdapter
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.Status
import com.example.navsample.dto.UserItemAdapterArgument
import com.example.navsample.fragments.dialogs.EditTextDialog

class ElementOperationHelper(
    private val algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    private val algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    private val algorithmNamesAdapter: AlgorithmItemListAdapter,
    private val algorithmPricesAdapter: AlgorithmItemListAdapter,
    private val userPrices: ArrayList<UserItemAdapterArgument>,
    private val userNames: ArrayList<UserItemAdapterArgument>,
    private val userNamesAdapter: UserItemListAdapter,
    private val userPricesAdapter: UserItemListAdapter
) {
    private var checkedElementsCounter = 0

    fun editSingleAlgorithmElement(
        position: Int,
        list: ArrayList<AlgorithmItemAdapterArgument>,
        adapter: AlgorithmItemListAdapter,
        editTextDialog: (EditTextDialog) -> Unit
    ) {
        if (position >= 0 && list[position].status != Status.BLOCKED) {
            editTextDialog.invoke(
                EditTextDialog(
                    list[position].value
                ) { text ->
                    list[position].value = text
                    adapter.notifyItemChanged(position)
                })
        }
    }

    fun editSingleUserElement(
        position: Int,
        list: ArrayList<UserItemAdapterArgument>,
        adapter: UserItemListAdapter,
        editTextDialog: (EditTextDialog) -> Unit
    ) {
        if (position >= 0) {
            editTextDialog.invoke(
                EditTextDialog(
                    list[position].value
                ) { text ->
                    list[position].value = text
                    list[position].algorithmItem.value = text
                    refreshAlgPosition(list[position].algorithmItem)
                    adapter.notifyItemChanged(position)
                })
        }
    }

    private fun refreshAlgPosition(item: AlgorithmItemAdapterArgument) {
        val indexInNameList = findIndex(item, algorithmNames)
        val indexInPriceList = findIndex(item, algorithmPrices)
        if (indexInNameList != -1) {
            algorithmNamesAdapter.notifyItemChanged(indexInNameList)
        }
        if (indexInPriceList != -1) {
            algorithmPricesAdapter.notifyItemChanged(indexInPriceList)
        }
    }

    private fun findIndex(
        refObj: AlgorithmItemAdapterArgument, list: List<AlgorithmItemAdapterArgument>
    ): Int {
        for ((index, obj) in list.withIndex()) {
            if (obj === refObj) {
                return index
            }
        }
        return -1
    }

    fun checkAndUncheckSingleElement(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        if (list[position].status == Status.DEFAULT) {
            list[position].status = Status.CHOSEN
            list[position].number = checkedElementsCounter
            checkedElementsCounter += 1
        } else if (list[position].status == Status.CHOSEN) {
            list[position].status = Status.DEFAULT
            decrementNumberInCell(list[position].number, algorithmPrices, algorithmPricesAdapter)
            decrementNumberInCell(list[position].number, algorithmNames, algorithmNamesAdapter)
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
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )

            SortingElementAction.SWAP -> OperationSwap(
                algorithmPrices,
                algorithmNames,
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )

            SortingElementAction.CLEAR_VALUE -> OperationClearValue(
                algorithmPrices,
                algorithmNames,
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )

            SortingElementAction.CLEAR_STATUS -> OperationClearStatus(
                algorithmPrices,
                algorithmNames,
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )

            SortingElementAction.MERGE -> OperationMerge(
                algorithmPrices,
                algorithmNames,
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )

            SortingElementAction.UNCHECK -> OperationUncheck(
                algorithmPrices,
                algorithmNames,
                algorithmNamesAdapter,
                algorithmPricesAdapter
            )
        }
        operation.execute(checkedElementsCounter)
        checkedElementsCounter = 0
    }
}
