package com.example.navsample.sorting.operation

import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.adapters.sorting.SortingItemListAdapter
import com.example.navsample.adapters.sorting.UserItemListAdapter
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.ItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.fragments.dialogs.EditTextDialog
import kotlin.math.max

class ElementOperationHelper(
    private val algorithmPrices: ArrayList<AlgorithmItemAdapterArgument>,
    private val algorithmNames: ArrayList<AlgorithmItemAdapterArgument>,
    private val algorithmNamesAdapter: AlgorithmItemListAdapter,
    private val algorithmPricesAdapter: AlgorithmItemListAdapter,
    val userPrices: ArrayList<UserItemAdapterArgument>,
    val userNames: ArrayList<UserItemAdapterArgument>,
    private val userNamesAdapter: UserItemListAdapter,
    private val userPricesAdapter: UserItemListAdapter
) {
    private var checkedElementsCounter = 0
    private var priceClickedItems = 0
    private var nameClickedItems = 0

    fun convertUserElementsToLines(): List<String> {
        val namePricePairs =
            Operation.prepareListWithGivenSize(max(userPrices.size, userNames.size))
        userNames.forEachIndexed { index, item ->
            namePricePairs[index] += item.value
        }
        userPrices.forEachIndexed { index, item ->
            if (namePricePairs[index].isNotEmpty()) {
                namePricePairs[index] += " "
            }
            namePricePairs[index] += item.value
        }
        return namePricePairs
    }

    fun clickUserElement(
        list: ArrayList<UserItemAdapterArgument>, position: Int, adapter: UserItemListAdapter
    ) {
        if (position >= 0) {
            if (list[position].type == Type.NAME) {
                nameClickedItems -= 1
            } else {
                priceClickedItems -= 1
            }
            for (pos in position + 1..list.lastIndex) {
                list[pos].algorithmItem.number -= 1
                adapter.notifyItemChanged(pos)
            }


            list[position].algorithmItem.type = Type.UNDEFINED
            list[position].algorithmItem.status = Status.DEFAULT
            refreshAlgPosition(list[position].algorithmItem)
            list.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }

    fun clickAlgorithmElement(item: AlgorithmItemAdapterArgument, currentType: Type) {
        item.type = currentType
        if (item.type == Type.NAME) {
            item.number = nameClickedItems
            nameClickedItems += 1
        } else {
            item.number = priceClickedItems
            priceClickedItems += 1
        }

        val userItemAdapterArgument = UserItemAdapterArgument(
            item.value, item.type, item
        )
        if (item.type == Type.PRICE) {
            userPrices.add(userItemAdapterArgument)
            userPricesAdapter.notifyItemInserted(userPrices.lastIndex)
        } else {
            userNames.add(userItemAdapterArgument)
            userNamesAdapter.notifyItemInserted(userNames.lastIndex)

        }
        item.status = Status.BLOCKED
    }

    fun <E : ItemAdapterArgument> editSingleElement(
        position: Int,
        item: ItemAdapterArgument,
        adapter: SortingItemListAdapter<E>,
        editTextDialog: (EditTextDialog) -> Unit
    ) {
        if (position < 0) {
            return
        }
        if (item is AlgorithmItemAdapterArgument && item.status != Status.BLOCKED) {
            editTextDialog.invoke(
                prepareEditDialogForArgument(
                    position,
                    item,
                    adapter as AlgorithmItemListAdapter
                )
            )
        } else if (item is UserItemAdapterArgument) {
            editTextDialog.invoke(
                prepareEditDialogForUser(
                    position,
                    item,
                    adapter as UserItemListAdapter
                )
            )
        }
    }

    private fun prepareEditDialogForUser(
        position: Int,
        item: UserItemAdapterArgument,
        adapter: UserItemListAdapter,
    ): EditTextDialog {
        return EditTextDialog(
            item.value,
        ) { text ->
            item.value = text
            item.algorithmItem.value = text
            refreshAlgPosition(item.algorithmItem)
            adapter.notifyItemChanged(position)
        }
    }

    private fun prepareEditDialogForArgument(
        position: Int,
        item: AlgorithmItemAdapterArgument,
        adapter: AlgorithmItemListAdapter,
    ): EditTextDialog {
        return EditTextDialog(item.value,
            {
                adapter.recycleList.add(position, AlgorithmItemAdapterArgument())
                adapter.notifyItemInserted(position)
            },
            {
                adapter.recycleList.add(position + 1, AlgorithmItemAdapterArgument())
                adapter.notifyItemInserted(position + 1)
            },
            { text ->
                item.value = text
                adapter.notifyItemChanged(position)
            }
        )
    }

    fun checkAndUncheckSingleElement(item: AlgorithmItemAdapterArgument) {
        if (item.status == Status.DEFAULT) {
            item.status = Status.CHOSEN
            item.number = checkedElementsCounter
            checkedElementsCounter += 1
        } else if (item.status == Status.CHOSEN) {
            item.status = Status.DEFAULT
            decrementNumberInCell(item.number, algorithmPrices, algorithmPricesAdapter)
            decrementNumberInCell(item.number, algorithmNames, algorithmNamesAdapter)
            item.number = 0
            checkedElementsCounter -= 1
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
        item: AlgorithmItemAdapterArgument,
        list: List<AlgorithmItemAdapterArgument>
    ): Int {
        for ((index, obj) in list.withIndex()) {
            if (obj === item) {
                return index
            }
        }
        return -1
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
}
