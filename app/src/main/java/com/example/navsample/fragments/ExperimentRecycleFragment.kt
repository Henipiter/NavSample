package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.navsample.NonScrollableGridLayoutManager
import com.example.navsample.ReceiptParser
import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.adapters.UserItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.Action
import com.example.navsample.dto.Action.CLEAR
import com.example.navsample.dto.Action.DELETE
import com.example.navsample.dto.Action.EDIT
import com.example.navsample.dto.Action.MERGE
import com.example.navsample.dto.Action.NONE
import com.example.navsample.dto.Action.SWAP
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.ProductListMode
import com.example.navsample.dto.SortingElementMode
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.dto.UserItemAdapterArgument
import com.example.navsample.fragments.dialogs.EditTextDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import java.lang.Integer.max
import java.util.Collections

open class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var algorithmOrderedPricesAdapter: AlgorithmItemListAdapter
    private lateinit var algorithmOrderedNamesAdapter: AlgorithmItemListAdapter
    private lateinit var userOrderedPricesAdapter: UserItemListAdapter
    private lateinit var userOrderedNamesAdapter: UserItemListAdapter
    private var recycleListAlgorithmPrices = arrayListOf<AlgorithmItemAdapterArgument>()
    private var recycleListAlgorithmNames = arrayListOf<AlgorithmItemAdapterArgument>()
    private var recycleListUserPrices = arrayListOf<UserItemAdapterArgument>()
    private var recycleListUserNames = arrayListOf<UserItemAdapterArgument>()
    private var checkedElementsCounter = 0
    private lateinit var receiptParser: ReceiptParser
    private var productListMode = ProductListMode.SELECT
    private var action = NONE//recycler_view_event_receipts
    private var sortingMode = SortingElementMode.SWITCHING
    private var editingMode = false


    private var currentType = Type.NAME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initObserver() {
        receiptImageViewModel.bitmapCropped.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmapCropped.value != null) {
                    binding.receiptImageBig.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
                }
            }
        }
    }

    private fun changeCurrentType(type: Type) {
        when (type) {
            Type.PRICE -> {
                currentType = Type.PRICE
                binding.priceModeColor.visibility = View.VISIBLE
                binding.nameModeColor.visibility = View.INVISIBLE
            }

            else -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }
        }
    }

    private fun changeCurrentType() {
        when (currentType) {
            Type.PRICE -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }

            else -> {
                currentType = Type.PRICE
                binding.priceModeColor.visibility = View.VISIBLE
                binding.nameModeColor.visibility = View.INVISIBLE
            }
        }
    }

    private fun putItemIntoRightList(item: AlgorithmItemAdapterArgument) {
        val userItemAdapterArgument = UserItemAdapterArgument(
            item.value,
            currentType,
            item
        )
        if (currentType == Type.PRICE) {
            recycleListUserPrices.add(userItemAdapterArgument)
            userOrderedPricesAdapter.notifyItemChanged(recycleListUserPrices.lastIndex)
        } else {
            recycleListUserNames.add(userItemAdapterArgument)
            userOrderedNamesAdapter.notifyItemChanged(recycleListUserPrices.lastIndex)
        }
        item.status = Status.BLOCKED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        binding.editGrid.visibility = View.GONE
        receiptParser = ReceiptParser(
            receiptDataViewModel.receipt.value?.id ?: -1,            //throw NoReceiptIdException(),
            receiptDataViewModel.store.value?.defaultCategoryId ?: -1 // throw NoStoreIdException()
        )
        binding.switchingButton.isChecked = true
        binding.priceModeColor.visibility = View.INVISIBLE
        binding.nameModeColor.visibility = View.VISIBLE
        recycleListAlgorithmNames =
            receiptDataViewModel.algorithmOrderedNames.value ?: arrayListOf()
        recycleListAlgorithmPrices =
            receiptDataViewModel.algorithmOrderedPrices.value ?: arrayListOf()
        recycleListUserNames = receiptDataViewModel.userOrderedName.value ?: arrayListOf()
        recycleListUserPrices = receiptDataViewModel.userOrderedPrices.value ?: arrayListOf()


        binding.switchingButton.setOnClickListener {
            if (sortingMode == SortingElementMode.SWITCHING) {
                changeCurrentType()
            }
            sortingMode = SortingElementMode.SWITCHING
        }
        binding.nameOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.NAME_ONLY
            changeCurrentType(Type.NAME)
        }
        binding.priceOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.PRICE_ONLY
            changeCurrentType(Type.PRICE)
        }

        //ALGORITHM     PRICE
        algorithmOrderedPricesAdapter = AlgorithmItemListAdapter(
            recycleListAlgorithmPrices,
            { position ->
                if (recycleListAlgorithmPrices[position].type == Type.UNDEFINED) {
                    recycleListAlgorithmPrices[position].type = currentType
                    putItemIntoRightList(recycleListAlgorithmPrices[position])
                    if (sortingMode == SortingElementMode.SWITCHING) {
                        changeCurrentType()
                    }
                }
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }, { position ->

                if (recycleListAlgorithmPrices[position].status == Status.DEFAULT) {
                    recycleListAlgorithmPrices[position].status = Status.CHOSEN
                } else if (recycleListAlgorithmPrices[position].status == Status.CHOSEN) {
                    recycleListAlgorithmPrices[position].status = Status.DEFAULT
                }

                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            })

        //ALGORITHM     NAME
        algorithmOrderedNamesAdapter = AlgorithmItemListAdapter(
            recycleListAlgorithmNames,
            { position ->
                if (recycleListAlgorithmNames[position].type == Type.UNDEFINED) {
                    recycleListAlgorithmNames[position].type = currentType
                    putItemIntoRightList(recycleListAlgorithmNames[position])
                    if (sortingMode == SortingElementMode.SWITCHING) {
                        changeCurrentType()
                    }
                }
                algorithmOrderedNamesAdapter.notifyItemChanged(position)

            }, { position ->

                if (recycleListAlgorithmNames[position].status == Status.DEFAULT) {
                    recycleListAlgorithmNames[position].status = Status.CHOSEN
                } else if (recycleListAlgorithmNames[position].status == Status.CHOSEN) {
                    recycleListAlgorithmNames[position].status = Status.DEFAULT
                }
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            })

        //USER     NAME
        userOrderedNamesAdapter = UserItemListAdapter(
            recycleListUserNames,
            { position ->
                if (position >= 0) {
                    recycleListUserNames[position].algorithmItem.type = Type.UNDEFINED
                    recycleListUserNames[position].algorithmItem.status = Status.DEFAULT
                    refreshAlgPosition(recycleListUserNames[position].algorithmItem)
                    recycleListUserNames.removeAt(position)
                    userOrderedNamesAdapter.notifyItemRemoved(position)
                }
            }) { position ->
            EditTextDialog(
                recycleListUserNames[position].value
            ) { text ->
                recycleListUserNames[position].value = text
                recycleListUserNames[position].algorithmItem.value = text
                refreshAlgPosition(recycleListUserNames[position].algorithmItem)
                userOrderedNamesAdapter.notifyItemChanged(position)
            }.show(childFragmentManager, "TAG")
        }

        //USER     PRICE
        userOrderedPricesAdapter = UserItemListAdapter(
            recycleListUserPrices,
            { position ->
                if (position >= 0) {
                    recycleListUserPrices[position].algorithmItem.type = Type.UNDEFINED
                    recycleListUserPrices[position].algorithmItem.status = Status.DEFAULT
                    refreshAlgPosition(recycleListUserPrices[position].algorithmItem)
                    recycleListUserPrices.removeAt(position)
                    userOrderedPricesAdapter.notifyItemRemoved(position)
                }
            }) { position ->
            EditTextDialog(
                recycleListUserPrices[position].value
            ) { text ->
                recycleListUserPrices[position].value = text
                recycleListUserPrices[position].algorithmItem.value = text
                refreshAlgPosition(recycleListUserPrices[position].algorithmItem)
                userOrderedPricesAdapter.notifyItemChanged(position)
            }.show(childFragmentManager, "TAG")
        }

        binding.recyclerViewUserName.adapter = userOrderedNamesAdapter
        binding.recyclerViewUserName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewUserPrice.adapter = userOrderedPricesAdapter
        binding.recyclerViewUserPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmName.adapter = algorithmOrderedPricesAdapter
        binding.recyclerViewAlgorithmName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmPrice.adapter = algorithmOrderedNamesAdapter
        binding.recyclerViewAlgorithmPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)

        binding.resetButton.setOnClickListener {

            val newPricesList = receiptDataViewModel.algorithmOrderedPrices.value?.let {
                ArrayList(it)
            } ?: arrayListOf()
            algorithmOrderedPricesAdapter.setNewData(newPricesList)
            val newNamesList = receiptDataViewModel.algorithmOrderedNames.value?.let {
                ArrayList(it)
            } ?: arrayListOf()
            algorithmOrderedNamesAdapter.setNewData(newNamesList)

            uncheckAll()
        }

        binding.cancelButton.setOnClickListener {
            binding.editButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.GONE
            binding.editGrid.visibility = View.GONE
            editingMode = false
            uncheckAll()
        }
        binding.editButton.setOnClickListener {
            binding.editButton.visibility = View.GONE
            binding.cancelButton.visibility = View.VISIBLE
            binding.editGrid.visibility = View.VISIBLE
            editingMode = true
        }

        binding.deleteButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(DELETE)
        }

        binding.clearButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(CLEAR)
        }
        binding.mergeButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(MERGE)
        }
        binding.swapSelectedButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(SWAP)
        }

        binding.confirmButton.setOnClickListener {
//            val namePricePairs = arrayListOf<String>()
//            for (i in 0..recycleListAlgorithmPrices.lastIndex step 2) {
//                var value = recycleListAlgorithmPrices[i].value
//                if (i + 1 <= recycleListAlgorithmPrices.lastIndex) {
//                    value += " " + recycleListPrices[i + 1].value
//                }
//                if (value != "") {
//                    namePricePairs.add(value)
//                }
//            }
//            namePricePairs.forEach { Log.d("ImageProcess", it) }
//            receiptDataViewModel.product.value = receiptParser.parseToProducts(namePricePairs)
//            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun findIndex(
        refObj: AlgorithmItemAdapterArgument,
        list: List<AlgorithmItemAdapterArgument>
    ): Int {
        for ((index, obj) in list.withIndex()) {
            if (obj === refObj) {
                return index
            }
        }
        return -1
    }

    private fun refreshAlgPosition(item: AlgorithmItemAdapterArgument) {
        val indexInNameList = findIndex(item, algorithmOrderedNamesAdapter.recycleList)
        val indexInPriceList = findIndex(item, algorithmOrderedPricesAdapter.recycleList)
        if (indexInNameList != -1) {
            algorithmOrderedNamesAdapter.notifyItemChanged(indexInNameList)
        }
        if (indexInPriceList != -1) {
            algorithmOrderedPricesAdapter.notifyItemChanged(indexInPriceList)
        }
    }

    private fun runActionView(action: Action) {
        this.action = action
        execute()
        uncheckAll()
    }

    private fun execute() {
        val checkedElements = getIndexOfChoseItems()
        when (action) {
            DELETE -> {
                val leftSide =
                    recycleListAlgorithmPrices.filterIndexed { index, _ -> index % 2 == 0 }
                        .toMutableList()
                val rightSide =
                    recycleListAlgorithmPrices.filterIndexed { index, _ -> index % 2 == 1 }
                        .toMutableList()

                val indicesDescending = checkedElements.sortedDescending()
                val newList = mutableListOf<AlgorithmItemAdapterArgument>()

                indicesDescending.forEach {
                    if (it % 2 == 0) {
                        leftSide.removeAt(it / 2)
                    } else {
                        rightSide.removeAt(it / 2)
                    }
                }
                for (i in 0..max(leftSide.lastIndex, rightSide.lastIndex)) {
                    if (i <= leftSide.lastIndex) {
                        newList.add(leftSide[i])
                    } else {
                        newList.add(AlgorithmItemAdapterArgument())
                    }
                    if (i <= rightSide.lastIndex) {
                        newList.add(rightSide[i])
                    } else {
                        newList.add(AlgorithmItemAdapterArgument())
                    }
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            SWAP -> {
                val newList = recycleListAlgorithmPrices.toMutableList()
                for (i in 1..checkedElements.lastIndex step 2) {
                    Collections.swap(newList, checkedElements[i - 1], checkedElements[i])
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            CLEAR -> {
                val newList = recycleListAlgorithmPrices.toMutableList()
                for (i in checkedElements.lastIndex downTo 0) {
                    newList[checkedElements[i]] =
                        AlgorithmItemAdapterArgument(" ", newList[checkedElements[i]].type)
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            MERGE -> {
                val newList = recycleListAlgorithmPrices.toMutableList()
                val firstIndex = checkedElements[0]
                var text = ""
                for (i in checkedElements.lastIndex downTo 0) {
                    text = newList[checkedElements[i]].value + " " + text
                }
                newList[firstIndex] =
                    AlgorithmItemAdapterArgument(text, newList[firstIndex].type)
                checkedElements.remove(firstIndex)
                val indicesDescending = checkedElements.sortedDescending()
                indicesDescending.forEach {
                    newList.removeAt(it)
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            EDIT -> {}
            NONE -> {}
        }
        this.action = NONE

    }

    private fun uncheckAll() {
        checkedElementsCounter = 0
        val newList = arrayListOf<AlgorithmItemAdapterArgument>()
        recycleListAlgorithmPrices.forEach { item ->
            val argument = AlgorithmItemAdapterArgument(item)
            argument.status = Status.DEFAULT
            argument.number = 0
            newList.add(argument)
        }
        algorithmOrderedPricesAdapter.setNewData(newList)
    }

    private fun getIndexOfChoseItems(): ArrayList<Int> {
        val list = arrayListOf<Int>()
        for (i in 1..checkedElementsCounter) {
            list.add(0)
        }
        for (i in 0..recycleListAlgorithmPrices.lastIndex) {
            val currentNumber = recycleListAlgorithmPrices[i].number
            if (currentNumber > 0) {
                list[currentNumber - 1] = i
            }
        }
        return list
    }
}
